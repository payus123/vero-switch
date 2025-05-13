package com.vero.coreprocessor.utils;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Getter
public class Wrapper<T> {
    private final String message;
    private final T data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Meta meta;



    public Wrapper(String message) {
        this(message, null, null);
    }

    public Wrapper(T data) {
        this(data, null);
    }

    public Wrapper(String message, T data) {
        this(message, data, null);
    }

    public Wrapper(T data, Meta meta) {
        this("Operation Successful!", data, meta);
    }

    public Wrapper(String message, T data, Meta meta) {
        this.message = message;
        this.data = data;
        this.meta = meta;
    }

    public record Meta(long currentPage, long from, long to, long perPage, long total, long lastPage) { }
}
