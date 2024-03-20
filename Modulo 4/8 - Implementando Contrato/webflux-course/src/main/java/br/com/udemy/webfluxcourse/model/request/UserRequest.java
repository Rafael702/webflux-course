package br.com.udemy.webfluxcourse.model.request;

public record UserRequest(
        String name,
        String email,
        String password
) {}
