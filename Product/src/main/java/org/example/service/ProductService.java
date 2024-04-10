package org.example.service;



import org.example.dto.ProductDetailRes;
import org.example.dto.SuccessRes;
import org.example.dto.ProductDto;
import org.example.entity.Product;
import org.example.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.member.MemberFeign;
import org.example.service.storage.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository ;
    private final MemberFeign memberFeign;
    private final StorageService storageService;
    private final String googleURL = "https://storage.googleapis.com/darakban-img/";

    public ResponseEntity<SuccessRes> addProduct(ProductDto productDto, String email, MultipartFile img_product, MultipartFile img_real) throws IOException {
        String nickName= memberFeign.getNickName(email);
        String profile = memberFeign.getProfile(email);
        productDto.setNick_name(nickName);
        productDto.setUserProfile(profile);
        // 이미지 구글 클라우드 저장
//        InputStream keyFile = ResourceUtils.getURL("classpath:darakbang-3b7415068a92.json" ).openStream();

        String product_file_name= storageService.imageUpload(img_product);
        String real_file_name= storageService.imageUpload(img_real);

        productDto.setImage_product(googleURL+product_file_name);
        productDto.setImage_real(googleURL+real_file_name);
        Product product = Product.ToEntity(productDto,email);
        productRepository.save(product);
        return ResponseEntity.ok(new SuccessRes(product.getProductName(),"success"));
    }


    public ResponseEntity<Page<ProductDto>> findProductPage (int page){
        Pageable pageable = PageRequest.of(page, 9, Sort.by(Sort.Direction.ASC, "productId"));
        Page<Product> productPage = productRepository.findAll(pageable);
        Page<ProductDto> products = productPage.map(ProductDto::ToDto);
        return ResponseEntity.ok(products);
    }

    public ResponseEntity<SuccessRes> deleteProduct(Long productId, String email) throws IOException {
        Optional<Product> product = productRepository.findByProductId(productId);
        if (product.isPresent()){
            if (product.get().getUserEmail().equals(email)){
                storageService.realImageDelete(productId);
                storageService.productImageDelete(productId);
                productRepository.delete(product.get());
                return ResponseEntity.ok(new SuccessRes(product.get().getProductName(),"삭제 성공"));
            }
            else {return ResponseEntity.ok(new SuccessRes(product.get().getProductName(),"등록한 이메일과 일치하지 않습니다."));}
        }
        else {
            return ResponseEntity.ok(new SuccessRes("","해당 상품이 없습니다"));
        }
    }

    public ResponseEntity<ProductDetailRes> findProductDetail(Long productId)
    {
        Optional<Product> selectedProduct = productRepository.findByProductId(productId);
        // 해당 상품 상세를 확인합니다.
        if (selectedProduct.isEmpty()){return ResponseEntity.noContent().build();}
        else {
            String keywords = selectedProduct.get().getProductName();
            // 해당 상품의 명을 확인합니다.

            Map<Product, Integer> resultMap = new HashMap<>();
            String[] words = StringUtils.tokenizeToStringArray(keywords, " ");
            // 해당 상품명을 띄어쓰기 기준 분할합니다.

            for (String word : words) {
                List<Product> similarProducts = productRepository.findByProductNameKeyword(word,productId);

                for (Product product : similarProducts) {
                    int count = resultMap.getOrDefault(product, 0);
                    resultMap.put(product, count + 1);
                }
            }
            // 복잡도가 조금 고민이 되지만.. 가장 많이 나오는 키워드의 상품을 hashmap에 append합니다.

            List<Map.Entry<Product, Integer>> sortedEntries = new ArrayList<>(resultMap.entrySet());
            sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder())); // value대로 정렬했습니다.
            //comparator에서 오류가 나서, (product가 comparotor 불가) 변경했습니다.

            List<Product> productList = new ArrayList<>();
            for (Map.Entry<Product, Integer> entry : sortedEntries) {
                productList.add(entry.getKey());
            } //한번이라도 검색되는 product들을 list로 변경했습니다.

            List<Product> topProducts = productList.subList(0, Math.min(productList.size(), 9));
            if (topProducts.isEmpty()) {
                List<Product> samecategoryproductlist = productRepository.findByProductCategory(selectedProduct.get().getCategoryId(), productId) ;
                topProducts = samecategoryproductlist.subList(0,Math.min(samecategoryproductlist.size(), 9)) ;
            } //검색이 하나도 안된다면.. 카테고리 위주로 검색한 결과를 return합니다.
            //9개보다 모자라면, 일단 있는걸 다 list로 return합니다.

            ProductDetailRes productDetailRes = new ProductDetailRes();
            productDetailRes.setProduct(selectedProduct.get());
            productDetailRes.setProductList(topProducts);
            return ResponseEntity.ok(productDetailRes);
        }

    }
    @Transactional
    public ResponseEntity<SuccessRes> updateProduct(Long productId, ProductDto productDto,String email,MultipartFile imageProduct,MultipartFile imageReal) throws IOException {
        Optional<Product> product=productRepository.findByProductId(productId);
        if (product.isEmpty()){return ResponseEntity.ok(new SuccessRes("","해당 상품이 없습니다"));}
        else {
            if (product.get().getUserEmail().equals(email)){
                if (!imageProduct.isEmpty()){
                    storageService.productImageDelete(productId);
                    String fileNameProduct = storageService.imageUpload(imageProduct);
                    productDto.setImage_product(googleURL+fileNameProduct);
                }
                if (!imageReal.isEmpty()){
                    storageService.realImageDelete(productId);
                    String fileNameReal = storageService.imageUpload(imageReal);
                    productDto.setImage_real(googleURL+fileNameReal);
                }
                productRepository.updateProduct(productId,productDto.getProduct_name(),productDto.getPrice(),
                        productDto.getImage_product(), productDto.getImage_real(),
                        productDto.getCategory_id(), productDto.getExpire_at());
                return ResponseEntity.ok(new SuccessRes(product.get().getProductName(),"수정 성공"));
            }
            else {return ResponseEntity.ok(new SuccessRes(product.get().getProductName(),"등록한 이메일과 일치하지않습니다."));}
        }
    }


}