package com.hhn.paulc.twittersentimentanalysis;

/**
 * Created by Paul on 10.04.2017.
 */

public class UserModel {

    public String m_email;

    // dummy Methode so that Firebase can import the Users in the Database.
    public UserModel(){}

    public UserModel(String email) {
        m_email = email;

    }
}
