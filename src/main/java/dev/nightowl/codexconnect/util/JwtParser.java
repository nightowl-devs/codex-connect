package dev.nightowl.codexconnect.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.nightowl.codexconnect.model.auth.AccountInfo;

public class JwtParser {

    public static AccountInfo extractAccountInfo(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            DecodedJWT jwt = JWT.decode(token);
//{"aud":["https://api.openai.com/v1"],"client_id":"app_EMoamEEZ73f0CkXaXp7hrann","exp":1781686145,"https://api.openai.com/auth":{"chatgpt_account_id":"392ebfce-f00d-4f5c-aac3-55d61d4fa9f2","chatgpt_account_user_id":"user-iyjN48FShbaaQvc9WQDKU3tI__392ebfce-f00d-4f5c-aac3-55d61d4fa9f2","chatgpt_compute_residency":"no_constraint","chatgpt_plan_type":"free","chatgpt_user_id":"user-iyjN48FShbaaQvc9WQDKU3tI","localhost":true,"user_id":"user-iyjN48FShbaaQvc9WQDKU3tI"},"https://api.openai.com/profile":{"email":"strona.febro@gmail.com","email_verified":true},"iat":1780822145,"iss":"https://auth.openai.com","jti":"55babca6-8225-4b2c-b5a0-3f1ab0a0b871","nbf":1780822145,"pwd_auth_time":1780822143942,"scp":["openid","profile","email","offline_access"],"session_id":"authsess_xVhRYh8pf5sKqh8AMGlMuxo4","sl":true,"sub":"google-oauth2|103869251730553413709"}


            Claim auth = jwt.getClaim("https://api.openai.com/auth");
            Claim profile = jwt.getClaim("https://api.openai.com/profile");

            String accountId = auth.asMap().get("chatgpt_account_id").toString();
            String email = profile.asMap().get("email").toString();
            String plan = auth.asMap().get("chatgpt_plan_type").toString();


            return AccountInfo.builder()
                    .accountId(accountId)
                    .email(email)
                    .planType(plan)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
