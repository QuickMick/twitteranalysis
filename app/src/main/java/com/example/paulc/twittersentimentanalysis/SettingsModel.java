package com.example.paulc.twittersentimentanalysis;

/**
 * Created by Paul on 22.04.2017.
 */

public class SettingsModel {

    public String m_consumerkeytxt;
    public String m_consumerkeytxtscrt;
    public String m_accesstokentxt;
    public String m_accesstokentxtscrt;

    // dummy Methode so that Firebase can import the Users in the Database.
    public SettingsModel(){}

    public SettingsModel(String consumerkeytext, String consumerkeytextscrt, String accesstokentext, String accesstokentextscrt) {
        m_consumerkeytxt = consumerkeytext;
        m_consumerkeytxtscrt = consumerkeytextscrt;
        m_accesstokentxt = accesstokentext;
        m_accesstokentxtscrt = accesstokentextscrt;

    }
}
