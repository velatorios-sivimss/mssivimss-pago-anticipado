package com.imss.sivimss.pagoanticipado.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Join {
    private final String tipo;
    private final String tabla;

    private List<String> on = new ArrayList<>();

    public Join(String tipo, String tabla, String... on) {
        this.tipo = tipo;
        this.tabla = tabla;
        this.on.addAll(Arrays.asList(on));
    }

    public void setOn(List<String> on) {
        this.on = on;
    }

    public void addOnCondition(String condition) {
        this.on.add(condition);
    }

}
