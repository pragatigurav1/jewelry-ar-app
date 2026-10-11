package com.example.jewelryar;

public class LoginResponse {
    public boolean success;
    public String message;
    public User user;

    public static class User {
        public int id;
        public String name;
        public String email;
    }
}