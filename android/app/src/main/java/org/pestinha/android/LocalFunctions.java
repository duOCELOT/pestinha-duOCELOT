package org.pestinha.android;
import org.json.JSONObject;
/** Typed seam for future small on-device providers. No model/runtime/network is bundled. */
final class LocalFunctions {
    interface Provider { Suggestion suggest(JSONObject care) throws Exception; String id(); }
    static final class Suggestion {final String action,text;Suggestion(String a,String t){action=a;text=t;}}
    static final class Rules implements Provider {
        public String id(){return "regras-locais";}
        public Suggestion suggest(JSONObject c){if(c.optDouble("illness")>.2)return new Suggestion("treat","Que tal cuidado e descanso para o pet?");if(c.optDouble("temperature")<-.3)return new Suggestion("warm","Seu pet parece sentir frio nesta simulação.");if(c.optDouble("temperature")>.3)return new Suggestion("cool","Uma sombra pode melhorar o conforto do pet.");if(c.optDouble("hunger")>.4)return new Suggestion("feed","Parece uma boa hora para um lanche virtual.");return new Suggestion("none","O pet está confortável. Que tal brincar?");}
    }
    static Suggestion request(Provider provider,JSONObject care,boolean consent) throws Exception {
        if(!consent)throw new IllegalStateException("Ative as sugestões locais primeiro.");
        JSONObject limited=new JSONObject();for(String key:new String[]{"hunger","illness","temperature"}){double n=care.optDouble(key,0);if(Double.isNaN(n)||Double.isInfinite(n))n=0;limited.put(key,Math.max(-1,Math.min(1,n)));}
        Suggestion s=provider.suggest(limited);if(s==null||!java.util.Arrays.asList("feed","treat","warm","cool","none").contains(s.action)||s.text==null||s.text.length()>300)throw new IllegalArgumentException("Resposta de função inválida.");return s;
    }
}
