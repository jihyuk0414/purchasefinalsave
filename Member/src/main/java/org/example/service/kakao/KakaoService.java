package org.example.service.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.parser.ParseException;
import org.example.dto.MemberDto;
import org.example.entity.Member;
import org.example.jwt.JwtDto;
import org.example.jwt.JwtProvider;
import org.example.jwt.KakaoToken;
import org.example.repository.member.MemberRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoService {
    private final KakaoApi kakaoApi;
    private final KakaoFeign kakaoFeign;
    private final MemberRepository memberRepository;
    private final AuthenticationProvider authenticationProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    private final String Content_type ="application/x-www-form-urlencoded;charset=utf-8";
    private final String grant_type = "authorization_code";
    private final String client_id = "b9759cba8e0cdd5bcdb9d601f5a10ac1";
    private final String login_redirect ="http://localhost:3000/user/login/oauth2/kakao";
    private final String logout_redirect ="http://localhost:8080";
    private final String secret ="8VCVTZpYOA21l7wgaKiqQa74q02S6pYI";
    public JwtDto GenerateToken(String code) throws ParseException, IOException, org.json.simple.parser.ParseException {
        String email = OAuthSignUp(code);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email,"default1234");
        Authentication authentication = authenticationProvider.authenticate(token);
        return jwtProvider.createToken(authentication);
    }

    public KakaoToken getToken(String code) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoToken kakaoToken=objectMapper.readValue(kakaoFeign.getAccessToken(Content_type,grant_type,client_id,login_redirect,code,secret), KakaoToken.class);
        log.info(kakaoToken.toString());
        return kakaoToken;
    }
    public String getkakaoInfo(String code) throws ParseException, JsonProcessingException {

        return kakaoApi.getUSerInfo("Bearer "+getToken(code).getAccessToken());

    }
    @Transactional
    public String OAuthSignUp(String code) throws ParseException, IOException, org.json.simple.parser.ParseException {
        String user = getkakaoInfo(code);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(user);
        JSONObject kakaoAccount=(JSONObject) jsonObject.get("kakao_account");
        JSONObject properties=(JSONObject) jsonObject.get("properties");
        MemberDto memberDto =MemberDto.builder()
                .email(kakaoAccount.get("email").toString())
                .image(properties.get("profile_image").toString())
                .nickName(properties.get("nickname").toString())
                .name(properties.get("nickname").toString())
                .password(passwordEncoder.encode("default1234"))
                .build();
        log.info(passwordEncoder.encode("default1234"));
        Optional<Member> member = memberRepository.findByEmail(memberDto.getEmail());
        Member member1 = Member.builder()
                .memberDto(memberDto)
                .build();
        if (member.isEmpty()){
            memberRepository.save(member1);
        }
       else {memberRepository.updateInfo(member1);}
       log.info("User DB 저장");

       return memberDto.getEmail();
    }
}
