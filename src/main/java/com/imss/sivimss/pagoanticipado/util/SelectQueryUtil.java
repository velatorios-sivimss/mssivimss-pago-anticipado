package com.imss.sivimss.pagoanticipado.util;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utiler&iacute;a para crear consultas select.
 *
 * @author esa
 * @version 1.0.2
 */
public class SelectQueryUtil {
    // constantes
    private static final String SELECT = "SELECT";
    private static final String SPACE = " ";
    private static final String FROM = "FROM";
    private static final String WHERE = "WHERE";
    private static final String LEFT_JOIN = "LEFT JOIN";
    private static final String INNER_JOIN = "INNER JOIN";
    private static final String JOIN = "JOIN";
    private static final String ON = "ON";
    private static final String OR = "OR";
    private static final String AND = "AND";
    private static final String ASTERISKS = "*";
    private static final String COLON = ":";
    private static final String ORDER_BY = "ORDER BY";
    private static final String GROUP_BY = "GROUP BY";
    private static final String LIMIT = "LIMIT";
    private static final String UNION = "UNION";
    private static final String ALL = "ALL";
    private static final String UNION_ALL = UNION + " " + ALL;
    private static final String EXCEPT = "EXCEPT";
    private static final String EXCEPT_ALL = EXCEPT + " " + ALL;
    private static final String INTERSECT = "INTERSECT";
    private static final String INTERSECT_ALL = INTERSECT + " " + ALL;
    // campos
    private final List<String> tablas = new ArrayList<>();
    private List<String> columnas = new ArrayList<>();
    private List<String> condiciones = new ArrayList<>();
    private Map<String, Object> parametros = new HashMap<>();
    private List<Join> joins = new ArrayList<>();
    private List<String> orderBy = new ArrayList<>();
    private List<String> groupBy = new ArrayList<>();
    private String lastMethodCalled = "";
    private Join helperJoin;
    private boolean isFromCalled;
    private boolean isSelectCalled;
    private boolean isJoinCalled;
    private Integer limit;

    /**
     * La funci&oacute;n <b>{@code select()}</b>, se tiene que invocar 2 veces, la
     * primera es para crear una instancia de
     * <b>QueryUtil</b> y se llama sin pasar ning&uacute;n argumento, la segunda
     * invocaci&oacute;n, en caso de ser
     * necesario, se pasan la lista de columnas que se van a usar para la consulta.
     * <p>
     * Los valores que recibe la funci&oacute;n puede ir de 1 a N, son valores
     * separados por comas y en caso de
     * que se requieran todos los campos, la funci&oacute;n puede ir vac&iacte;a.
     * Por ejemplo:
     * <p>
     * - <b>{@code select("columna_1 as id")}</b>
     * <p>
     * - <b>{@code select("columna_1 as id", "columna_2 as nombre", ...)}</b>
     * <p>
     * - <b>{@code select()}</b>
     *
     * @param columnas Lista de columnas, dichas columnas representan los valores
     *                 que se van a recuperar
     *                 de la consulta.
     * @return Regresa la misma instancia para que se le puedan agregar m&aacute;s
     *         funciones.
     * @since 1.0.0
     */
    public SelectQueryUtil select(String... columnas) {
        if (isFromCalled) {
            throw new IllegalStateException("No se puede llamar el from antes del select");
        }
        this.columnas = Arrays.asList(columnas);
        lastMethodCalled = SELECT;
        isSelectCalled = true;
        return this;
    }

    /**
     * Agrega la tabla o tablas necesarias para la consulta que se est&eacute;
     * armando.
     * <p>
     * Se puede agregar, por ejemplo:
     * <p>
     * - Para solo una tabla: <b>{@code from("USUARIO as usuario")}</b>
     * <p>
     * - Para varias tablas: <b>{@code from("USUARIO as usuario", "ROL as rol")}</b>
     *
     * @param tabla Es una cadena que representa la o las tablas a las que va a
     *              realizar la consulta
     * @return Regresa la misma instancia para que se puedan anidar las otras
     *         funciones
     * @since 1.0.0
     */
    public SelectQueryUtil from(String... tabla) {
        if (!isSelectCalled) {
            throw new IllegalStateException("No se puede llamar from sin haber agregado la sentencia select");
        }
        this.tablas.addAll(Arrays.asList(tabla));
        isFromCalled = true;
        lastMethodCalled = FROM;
        return this;
    }

    /**
     * La funci&oacute;n `where` se usa para agregar condiciones, estas pueden ser
     * agregadas separadas por comas.
     * <p>
     * La sentencia where de sql se agrega hasta que se manda llamar la
     * funci&oacute;n <b>{@code build()}</b>
     * <p>
     * Todas las condiciones que se agreguen mediante esta funci&oacute;n se
     * anidar&aacute;n con un
     * operador <b>{@code AND}</b>
     *
     * @param condiciones Lista de condiciones que se van a evaluar en el query
     * @return
     * @since 1.0.0
     */
    public SelectQueryUtil where(String... condiciones) {
        if (this.condiciones == null) {
            this.condiciones = new ArrayList<>();
        }

        this.condiciones.addAll(Arrays.asList(condiciones));
        lastMethodCalled = WHERE;
        return this;
    }

    /**
     * Agrega solo una condici&oacute;n a diferencia de
     * <b>{@code where(String... condiciones}</b> esta
     * funci&oacute;n recibe solo una condici&oacute;n.
     *
     * @param condicion
     * @return
     * @since 1.0.0
     */
    public SelectQueryUtil where(String condicion) {
        if (condiciones == null) {
            this.condiciones = new ArrayList<>();
        }
        this.condiciones.add(condicion);
        lastMethodCalled = WHERE;
        return this;
    }

    /**
     * Agrega el operador <b>{@code AND}</b> a la condici&oacute;n
     * <p>
     * Se debe colocar despu&eacute;s de la llamada a una funci&oacute;n
     * <b>{@code where(...)}</b> o despu&eacute;s
     * de un <b>{@code join(...)}, {@code innerJoin(...)}, {@code leftJoin(...)}</b>
     *
     * @param condicion
     * @return
     * @since 1.0.0
     */
    public SelectQueryUtil and(String condicion) {
        return validarCondicionesOrAnd(condicion, AND);
    }

    /**
     * Agrega una condicion SQL OR.
     * <p>
     * Se debe colocar despu&eacute;s de la llamada a una funci&oacute;n
     * <b>{@code where(...)}</b> o despu&eacute;s
     * de un <b>{@code join(...)}, {@code innerJoin(...)}, {@code leftJoin(...)}</b>
     *
     * @param condicion
     * @return
     * @since 1.0.0
     */
    public SelectQueryUtil or(String condicion) {
        return validarCondicionesOrAnd(condicion, OR);
    }

    /**
     * Agrega una o varias condiciones <b>{@code OR}</b>
     *
     * @param condiciones lista de condiciones separadas por comas <b>{@code ,}</b>
     * @return la misma instancia para encadenar las funciones.
     * @see #or(String)
     * @since 1.0.1
     */
    public SelectQueryUtil or(String... condiciones) {
        if (condiciones.length == 0) {
            throw new IllegalStateException("Se debe agregar por lo menos una condicion como parametro");
        }
        for (String condicion : condiciones) {
            this.or(condicion);
        }
        return this;
    }

    /**
     * Valida si la la funci&oacute;n <b>{@code or(...)}</b> o la funci&oacute;n
     * <b>{@code and(...)}</b>
     *
     * @param condicion
     * @param or
     * @return
     * @since 1.0.0
     */
    private SelectQueryUtil validarCondicionesOrAnd(String condicion, String or) {
        if (Objects.equals(lastMethodCalled, WHERE)) {
            this.condiciones.add(crearCondicion(condicion, or));
        }
        if (lastMethodCalled.equals(JOIN)) {
            this.helperJoin.addOnCondition(crearCondicion(condicion, or));
        }
        return this;
    }

    /**
     * Agrega par&aacute;metros para que se pueda hacer la sustituci&oacute;n de
     * dicho elemento para armar el query
     * con los valores que se agreguen en el mapa de par&aacute;metros.
     *
     * @param nombre
     * @param valor
     * @return
     * @since 1.0.0
     */
    @SuppressWarnings("UnusedReturnValue")
    public SelectQueryUtil setParameter(String nombre, Object valor) {
        if (this.parametros == null) {
            this.parametros = new HashMap<>();
        }

        this.parametros.put(nombre, valor);
        return this;
    }

    /**
     * Agrega la sentencia SQL <b>{@code ORDER BY}</b> para ordenar la consulta.
     *
     * @param columna
     * @return
     * @since 1.0.0
     */
    @SuppressWarnings("UnusedReturnValue")
    public SelectQueryUtil orderBy(String columna) {
        this.orderBy.add(columna);
        return this;
    }

    /**
     * Agrega la sentencia SQL <b>{@code GROUP BY}</b> para la consulta.
     *
     * @param columna
     * @return
     * @since 1.0.0
     */
    public SelectQueryUtil groupBy(String columna) {
        this.groupBy.add(columna);
        return this;
    }

    /**
     * Agrega la sentencia de <b>{@code LEFT JOIN}</b> para hacer consultas con
     * otras tablas.
     *
     * @param tabla nombre de la tabla a la que se le va a hacer el join
     * @param on    lista de condiciones separadas por <b>{@code ,}</b>
     * @return
     * @since 1.0.0
     */
    public SelectQueryUtil leftJoin(String tabla, String... on) {
        helperJoin = new Join(LEFT_JOIN, tabla, on);
        joins.add(helperJoin);
        isJoinCalled = true;
        lastMethodCalled = JOIN;
        return this;
    }

    /**
     * Agrega la sentencia <b>{@code INNER JOIN}</b> para hacer consultas usando
     * otras tablas.
     *
     * @param tabla
     * @param on
     * @return
     * @since 1.0.0
     */
    public SelectQueryUtil innerJoin(String tabla, String... on) {
        helperJoin = new Join(INNER_JOIN, tabla, on);
        joins.add(helperJoin);
        isJoinCalled = true;
        lastMethodCalled = JOIN;
        return this;
    }

    /**
     * Agrega la sentencia <b>{@code JOIN}</b> para hacer consultas usando otras
     * tablas.
     *
     * @param tabla
     * @param on
     * @return
     * @since 1.0.0
     */
    public SelectQueryUtil join(String tabla, String... on) {
        helperJoin = new Join(JOIN, tabla, on);
        joins.add(helperJoin);
        isJoinCalled = true;
        lastMethodCalled = JOIN;
        return this;
    }

    /**
     * <b>{@code on(condiciones)}</b> es usado para agregar condiciones para una
     * sentencia <b>{@code JOIN}</b>
     * se puede combinar para agregar varias condiciones al <b>{@code JOIN}</b> en
     * cuesti&oacute;n.
     * <p>
     * Se debe llamar inmediatamente despu&eacute;s de la funci&oacute;n
     * <b>{@code join(...)}</b> o <b>{@code leftJoin(...)}</b>
     *
     * @param condiciones
     * @return
     * @since 1.0.0
     */
    public SelectQueryUtil on(String... condiciones) {
        if (!isJoinCalled && !lastMethodCalled.equals(JOIN)) {
            throw new IllegalStateException("on no puede se llamado sin antes invocar a join o joinLeft");
        }
        for (String condicion : condiciones) {
            helperJoin.addOnCondition(condicion);
        }
        return this;
    }

    /**
     * Agrega la sentencia limit al query que se est&aacute; armando.
     *
     * @param limit
     * @return
     * @since 1.0.1
     */
    public SelectQueryUtil limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Agrega una cla&uacute;sula <b>{@code UNION}</b> a la consulta actual, uniendo
     * el resultado de la consulta actual con la
     * consulta generada por el objeto SelectQueryUtil proporcionado.
     *
     * @param selectQuery el objeto SelectQueryUtil que representa la consulta a
     *                    unir
     * @return una cadena que representa la consulta actual con la cla&uacute;sula
     *         <b>{@code UNION}</b> y la consulta proporcionada unida
     * @throws IllegalStateException si no se ha agregado una sentencia SELECT
     *                               previamente
     * @since 1.0.1
     */
    public String union(SelectQueryUtil selectQuery) {
        validarSelectCalled();
        return this.union(selectQuery.build(), false);
    }

    /**
     * Agrega la cla&uacute;sula <b>{@code UNION}</b> a la consulta actual, une el
     * resultado del query actual
     * con un segundo query, para poder mantener el orden en el que se requiere
     * hacer el <b>{@code UNION}</b>
     *
     * @param selectQuery el objeto SelectQueryUtil que representa la consulta a
     *                    unir
     * @return una cadena que representa la consulta actual con la cla&uacute;sula
     *         <b>{@code UNION ALL}</b> y la consulta proporcionada
     *         unida
     * @see #union(SelectQueryUtil)
     * @since 1.0.2
     */
    public String unionAll(SelectQueryUtil selectQuery) {
        validarSelectCalled();
        return this.union(selectQuery.build(), true);
    }

    /**
     * Agrega la cla&uacute;sula <b>{@code EXCEPT}</b> a la consulta actual, une el
     * resultado del query actual
     * con un segundo query, para poder mantener el orden en el que se requiere
     * hacer el <b>{@code EXCEPT}</b>
     *
     * @param selectQuery el objeto SelectQueryUtil que representa la consulta a
     *                    concatenar a la primer consulta
     * @return una cadena que representa la consulta actual con la cla&uacute;sula
     *         <b>{@code EXCEPT}</b> y la consulta proporcionada
     *         juntas
     * @see #exceptAll(SelectQueryUtil)
     * @since 1.0.2
     */
    public String except(SelectQueryUtil selectQuery) {
        validarSelectCalled();
        return this.except(selectQuery.build(), false);
    }

    /**
     * Agrega la cla&uacute;sula <b>{@code EXCEPT ALL}</b> a la consulta actual, une
     * el resultado del query actual
     * con un segundo query, para poder mantener el orden en el que se requiere
     * hacer el <b>{@code EXCEPT ALL}</b>
     *
     * @param selectQuery el objeto SelectQueryUtil que representa la consulta a
     *                    concatenar a la primer consulta
     * @return una cadena que representa la consulta actual con la cla&uacute;sula
     *         <b>{@code EXCEPT ALL}</b> y la consulta proporcionada
     *         juntas
     * @see #except(SelectQueryUtil)
     * @since 1.0.2
     */
    public String exceptAll(SelectQueryUtil selectQuery) {
        validarSelectCalled();
        return this.except(selectQuery.build(), true);
    }

    /**
     * Agrega la cla&uacute;sula <b>{@code INTERSECT}</b> a la consulta actual, une
     * el resultado del query actual
     * con un segundo query, para poder mantener el orden en el que se requiere
     * hacer el <b>{@code INTERSECT}</b>
     *
     * @param selectQuery el objeto SelectQueryUtil que representa la consulta a
     *                    concatenar a la primer consulta
     * @return una cadena que representa la consulta actual con la cla&uacute;sula
     *         <b>{@code INTERSECT}</b> y la consulta proporcionada
     *         juntas
     * @see #intersectAll(SelectQueryUtil)
     * @since 1.0.2
     */
    public String intersect(SelectQueryUtil selectQuery) {
        validarSelectCalled();
        return this.intersect(selectQuery.build(), false);
    }

    /**
     * Agrega la cla&uacute;sula <b>{@code INTERSECT ALL}</b> a la consulta actual,
     * une el resultado del query actual
     * con un segundo query, para poder mantener el orden en el que se requiere
     * hacer el <b>{@code INTERSECT ALL}</b>
     *
     * @param selectQuery el objeto SelectQueryUtil que representa la consulta a
     *                    concatenar a la primer consulta
     * @return una cadena que representa la consulta actual con la cla&uacute;sula
     *         <b>{@code INTERSECT ALL}</b> y la consulta proporcionada
     *         juntas
     * @see #intersect(SelectQueryUtil)
     * @since 1.0.2
     */
    public String intersectAll(SelectQueryUtil selectQuery) {
        validarSelectCalled();
        return this.intersect(selectQuery.build(), true);
    }

    /**
     * Regresa el query que se construy&oacute;.
     * <p>
     *
     * @return La sentencia armada con los par&aacute;metros que se hayan agregado.
     * @since 1.0.0
     */
    public String build() {
        StringBuilder stringBuilder = new StringBuilder(SELECT);
        stringBuilder.append(SPACE);
        agregarColumnas(stringBuilder);
        agregarFrom(stringBuilder);
        agregarJoins(stringBuilder);
        agregarWhere(stringBuilder);
        agregarLimit(stringBuilder);
        addOrderBy(stringBuilder);
        addGroupBy(stringBuilder);

        return stringBuilder.toString();
    }

    /**
     * Recupera el query generado ya encriptado y usando utf-8
     *
     * @return
     * @since 1.0.2
     */
    public String encrypt(String query) throws UnsupportedEncodingException {
        return DatatypeConverter
                .printBase64Binary(query.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Recupera el query generado ya encriptado y usando utf-8
     *
     * @return
     * @since 1.0.2
     */
    public String encrypt(String query, String coding) throws UnsupportedEncodingException {
        return DatatypeConverter
                .printBase64Binary(query.getBytes(coding));
    }

    /**
     * Une la consulta actual con una consulta generada a partir de la cadena
     * proporcionada usando la cláusula
     * <b>{@code UNION}</b>.
     *
     * @param build la cadena que representa la consulta a unir
     * @return una cadena que representa la consulta actual con la cláusula UNION y
     *         la consulta proporcionada unida
     * @since 1.0.1
     */
    private String union(String build, boolean isAll) {
        final String queryUnion = this.build();
        return queryUnion + SPACE +
                (isAll ? UNION_ALL : UNION) +
                SPACE +
                build;
    }

    /**
     * Crea la sentencia <b>{@code EXCEPT}</b> o <b>{@code EXCEPT ALL}</b>
     *
     * @param build es la consulta que se va a agregar a la consulta actual
     * @param isAll es una bandera que verifica si se agrega o no la palaba
     *              <b>{@code ALL}</b>
     * @return Las sentencias con la instrucci&oacute;n <b>{@code EXCEPT}</b> o
     *         <b>{@code EXCEPT ALL}</b>
     * @since 1.0.2
     */
    private String except(String build, boolean isAll) {
        final String queryUnion = this.build();
        return queryUnion + SPACE +
                (isAll ? EXCEPT_ALL : EXCEPT) +
                SPACE +
                build;
    }

    /**
     * Crea la sentencia <b>{@code INTERSECT}</b> o <b>{@code INTERSECT ALL}</b>
     *
     * @param build es la consulta que se va a agregar a la consulta actual
     * @param isAll es una bandera que verifica si se agrega o no la palaba
     *              <b>{@code ALL}</b>
     * @return Las sentencias con la instrucci&oacute;n <b>{@code INTERSECT}</b> o
     *         <b>{@code EXCEPT ALL}</b>
     * @since 1.0.2
     */
    private String intersect(String build, boolean isAll) {
        final String queryUnion = this.build();
        return queryUnion + SPACE +
                (isAll ? INTERSECT_ALL : INTERSECT) +
                SPACE +
                build;
    }

    /**
     * Agrega la sentencia <b>{@code LIMIT}</b> a la sentencia <b>{@code SQL}</b>
     *
     * @param stringBuilder Cadena que se est&aacute; armando para agregar el
     *                      <b>{@code LIMIT}</b>
     * @since 1.0.1
     */
    private void agregarLimit(StringBuilder stringBuilder) {
        if (limit != null) {
            stringBuilder.append(SPACE)
                    .append(LIMIT)
                    .append(SPACE)
                    .append(limit.toString());
        }
    }

    /**
     * Agrega la sentencia <b>{@code FROM}</b> a la cadena para crear el <b>query
     * sql</b>.
     *
     * @param stringBuilder Cadena para ir armando la sentencia <b>{@code SQL}</b>
     * @since 1.0.0
     */
    private void agregarFrom(StringBuilder stringBuilder) {
        stringBuilder.append(FROM).append(SPACE);
        stringBuilder.append(String.join(", ", tablas)).append(SPACE);
    }

    /**
     * Agrega la sentencia <b>{@code WHERE}</b> a la cadena para armar el
     * query<b>{@code SQL}</b>.
     *
     * @param stringBuilder Cadena para ir armando la sentencia <b>{@code SQL}</b>
     * @since 1.0.0
     */
    private void agregarWhere(StringBuilder stringBuilder) {
        if (validarCondiciones()) {
            stringBuilder.append(SPACE).append(WHERE).append(SPACE);
            agregarCondicionesWhere(stringBuilder);
        }
    }

    /**
     * Agrega los par&aacute;metros a los placeholders de forma din&aacute;mica.
     *
     * @param stringBuilder     Cadena para ir armando la sentencia
     *                          <b>{@code SQL}</b>
     * @param index             &iacute;ndice para saber que condici&oacute;n se
     *                          est&aacute; procesando.
     * @param condicion         Condici&oacute;n que se est&aacute; evaluando.
     * @param helperCondiciones Lista auxiliar para manejar las condiciones.
     * @since 1.0.0
     */
    private void agregarParametros(StringBuilder stringBuilder, int index, String condicion,
            List<String> helperCondiciones) {

        if (helperCondiciones.isEmpty()) {
            helperCondiciones = this.condiciones;
        }
        final boolean contieneOr = condicion.contains("#" + OR);
        final boolean contieneAndOr = contieneOr || condicion.contains("#" + AND);
        if (index != 0 &&
                !contieneAndOr) {
            stringBuilder.append(SPACE).append(AND).append(SPACE);
        }
        if (condicion.contains(COLON)) {
            String nombreParametro = condicion.substring(condicion.indexOf(COLON) + 1);
            Object value = parametros.get(nombreParametro);
            if (value instanceof String) {
                condicion = condicion.replace(COLON + nombreParametro, "'" + value.toString() + "'");
            } else {
                condicion = condicion.replace(COLON + nombreParametro, value.toString());
            }
        } else {
            condicion = condicion.trim();
        }
        if (contieneAndOr) {
            condicion = condicion.replace("#", "");
        }

        stringBuilder.append(condicion).append(SPACE);
    }

    /**
     * Agrega la senetencia order by en caso de que se exista.
     *
     * @param stringBuilder Cadena para ir armando la sentencia <b>{@code SQL}</b>
     * @since 1.0.0
     */
    private void addOrderBy(StringBuilder stringBuilder) {
        if (!orderBy.isEmpty()) {
            stringBuilder.append(SPACE)
                    .append(ORDER_BY)
                    .append(SPACE)
                    .append(String.join(", ", orderBy))
                    .append(SPACE);
        }
    }

    /**
     * Agrega la sentencia group by en caso de que exista
     *
     * @param stringBuilder Cadena para ir armando la sentencia <b>{@code SQL}</b>
     * @since 1.0.0
     */
    private void addGroupBy(StringBuilder stringBuilder) {
        if (!groupBy.isEmpty()) {
            stringBuilder.append(SPACE)
                    .append(GROUP_BY)
                    .append(SPACE)
                    .append(String.join(", ", groupBy))
                    .append(SPACE);
        }
    }

    /**
     * Agrega la lista de condiciones que se hayan agregado a la consulta.
     *
     * @param stringBuilder Cadena para ir armando la sentencia <b>{@code SQL}</b>
     * @since 1.0.0
     */
    private void agregarCondicionesWhere(StringBuilder stringBuilder) {
        if (validarCondiciones()) {
            for (int index = 0; index < condiciones.size(); index++) {
                String condicion = condiciones.get(index);
                agregarParametros(stringBuilder, index, condicion, new ArrayList<>());
            }
        }
    }

    /**
     * Valida que la lista de condiciones tenga alg&uacute;n valor.
     *
     * @return
     * @since 1.0.0
     */
    private boolean validarCondiciones() {
        return !condiciones.isEmpty();
    }

    /**
     * Agrega los joins si es que se han agregado a la consulta.
     *
     * @param stringBuilder Cadena para ir armando la sentencia <b>{@code SQL}</b>
     * @since 1.0.0
     */
    private void agregarJoins(StringBuilder stringBuilder) {
        if (!joins.isEmpty()) {
            for (Join join : joins) {
                stringBuilder.append(SPACE);
                stringBuilder.append(join.getTipo());
                stringBuilder.append(SPACE);
                stringBuilder.append(join.getTabla());
                agregarCondicionesJoin(stringBuilder, join);
            }
        }
    }

    /**
     * Agrega la lista de condiciones para la sentencia <b>{@code JOIN}</b> que
     * corresponda
     *
     * @param stringBuilder Cadena para ir armando la sentencia <b>{@code SQL}</b>
     * @param join          Sentencia <b>{@code JOIN}</b> que se va a agregar.
     * @since 1.0.0
     */
    private void agregarCondicionesJoin(StringBuilder stringBuilder, Join join) {
        final List<String> condicionesTemp = join.getOn();
        if (!condicionesTemp.isEmpty()) {
            stringBuilder.append(SPACE).append(ON).append(SPACE);
            for (int index = 0; index < condicionesTemp.size(); index++) {
                String condicion = condicionesTemp.get(index);
                agregarParametros(stringBuilder, index, condicion, condicionesTemp);
            }
        }
    }

    /**
     * Agrega la lista de columnas para construir la consulta.
     *
     * @param stringBuilder Cadena para ir armando la sentencia <b>{@code SQL}</b>
     * @since 1.0.0
     */
    private void agregarColumnas(StringBuilder stringBuilder) {
        if (columnas.isEmpty()) {
            stringBuilder.append(ASTERISKS).append(SPACE);
        } else {
            stringBuilder.append(String.join(", ", columnas)).append(SPACE);
        }
    }

    /**
     * Agrega una nueva condici&oacute;n dependiendo del tipo:
     * <p>
     * - AND
     * - OR
     *
     * @param condicion Condici&oacute;n que se va a evaluar.
     * @param tipo      Operador l&oacute;gico.
     * @return La condici&oacute;n y el operador, puede ser un <b>{@code OR}</b> o
     *         <b>{@code AND}</b>.
     * @since 1.0.0
     */
    private static String crearCondicion(String condicion, String tipo) {
        return SPACE +
                "#" + tipo +
                SPACE +
                condicion;
    }

    /**
     * Valida si la funci&oacute;n <b>{@code select()}</b> ya se ha invocado.
     *
     * @see SelectQueryUtil#select(String...)
     * @since 1.0.2
     */
    private void validarSelectCalled() {
        if (!isSelectCalled) {
            throw new IllegalStateException("No se puede crear el query sin haber agregado la sentencia select");
        }
    }

}
