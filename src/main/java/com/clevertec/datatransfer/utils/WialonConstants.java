package com.clevertec.datatransfer.utils;

public class WialonConstants {

    public static final String WIALON_URL = "https://gps.cherkizovo.com/wialon/ajax.html";
    public static final String IMAGE_HOST = "https://gps.cherkizovo.com/avl_zone_image/";
    public static final String SID_KEY = "sid";
    public static final String SID_VALUE = "c69bf3834122a3e57d2b723e9d5fbc98";
    public static final String SVC_KEY = "svc";
    public static final String SVC_VALUE_DELETE_ZONES = "resource/update_zone";
    public static final String SVC_VALUE_CREATE_ZONE = "resource/update_zone";
    public static final String SVC_VALUE_CREATE_MULTIPLE_GEOZONES = "core/batch";
    public static final String SVC_VALUE_GET_GEOZONES_IDS = "core/search_items";
    public static final String SVC_VALUE_GET_GEOZONE_INFO = "core/batch";
    public static final String SVC_VALUE_DELETE_GEOZONES = "core/batch";
    public static final String SVC_VALUE_GET_GEOZONES_IDS_BY_NAME = "core/batch";
    public static final String SVC_TOKEN_LOGIN_VALUE = "token/login";
    public static final String PARAMS_KEY = "params";
    public static final String TOKEN_PARAMS = "{\"token\":\"%s\", \"fl\": 1}";
    public final static String LOG_NEW_SESSION = "New session is {}";
    public final static String LOG_REFRESH_SESSION = "Refresh session and repeat request to Wialon";
    public final static String LOG_ERROR_RESPONSE_FROM_WIALON = "Error response from Wialon - {}";
    public final static String LOG_REQUEST_TO_WIALON = "Request to Wialon - {}{}";
    public final static String ERROR_RESPONSE_FROM_WIALON = "Error response from Wialon: %s";
    public final static String EXPIRED_WIALON_SESSION = "Ошибка: Недействительная сессия";

    public static final String PARAMS_VALUE_GET_ALL_GEOZONES = "{" +
            "   \"spec\":" +
            "   {" +
            "       \"itemsType\":\"avl_resource\"," +
            "       \"propName\":\"\"," +
            "       \"propValueMask\":\"\"," +
            "       \"sortType\":\"\"" +
            "   }," +
            "   \"force\":1," +
            "   \"flags\":4097," +
            "   \"from\":0," +
            "   \"to\":0" +
            "}";
    /*
    public static final String GET_GEOZONE_INFO_PARAMS_TEMPLATE = "{" +
            "   \"itemId\":${itemId}," +
            "   \"col\":${ids}," +
            "   \"flags\":25" +
            "}";
     */

    public static final String GET_GEOZONE_INFO_ITEM_TEMPLATE = "{" +
            "\"svc\":\"resource/get_zone_data\"," +
            "\"params\":{\"itemId\": ${itemId}," +
            "\"col\":${ids}," +
            "\"flags\":25" +
            "}}";

    public static final String DELETE_ITEM_PARAMS_TEMPLATE = "{" +
            "   \"itemId\":${itemId}," +
            "   \"id\":${id}," +
            "   \"callMode\":\"delete\"" +
            "}";
    public static final String DELETE_ITEM_TEMPLATE = "{" +
            "   \"" + SVC_KEY + "\":\"${" + SVC_KEY + "}\"," +
            "   \"" + PARAMS_KEY + "\":${" + PARAMS_KEY + "}" +
            "}";


    public static final String CREATE_ITEM_PARAMS_TEMPLATE = "{\"itemId\":${itemId},\n" +
            "\t\t\t\t \"id\":0,\n" +
            "\t\t\t\t \"callMode\":\"create\",\n" +
            "\t\t\t\t \"n\":\"${title}\",\n" +
            "\t\t\t\t \"d\":\"${description}\",\n" +
            "\t\t\t\t \"t\":3,\n" +
            "\t\t\t\t \"w\":${radius},\n" +
            "\t\t\t\t \"f\":112,\n" +
            "\t\t\t\t \"c\":${zoneColor},\n" +
            "\t\t\t\t \"tc\":${signatureColor},\n" +
            "\t\t\t\t \"ts\":${fontHeight},\n" +
            "\t\t\t\t \"min\":1,\n" +
            "\t\t\t\t \"max\":${scale},\n" +
            "\t\t\t\t \"path\":\"library/poi/E_3.png\",\n" +
            "\t\t\t\t \"libId\":0,\n" +
            "\t\t\t\t \"jp\":{},\n" +
            "\t\t\t\t \"p\":[\t\t\t\t\n" +
            "\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\"x\":${longitude},\n" +
            "\t\t\t\t\t\t\"y\":${latitude},\n" +
            "\t\t\t\t\t\t\"r\":${radius}\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t ]}\n";

    public static final String CREATE_ITEM_TEMPLATE = "{" +
            "   \"" + SVC_KEY + "\":\"${" + SVC_KEY + "}\"," +
            "   \"" + PARAMS_KEY + "\":${" + PARAMS_KEY + "}" +
            "}";

    public static final String GET_PARENT_ID_BY_NAME_ITEM_PARAMS_TEMPLATE = "{" +
            "\"spec\":" +
            "   {" +
            "       \"itemsType\":\"avl_resource\"," +
            "       \"propName\":\"sys_name\"," +
            "       \"propValueMask\":\"${parentName}\"," +
            "       \"sortType\":\"sys_name\"" +
            "   }," +
            "   \"force\":1," +
            "   \"flags\":1," +
            "   \"from\":0," +
            "   \"to\":0" +
            "}";

    public static final String GET_PARENT_ID_BY_NAME_ITEM_TEMPLATE = "{" +
            "\"svc\":\"core/search_items\"," +
            "\"params\":${params}" +
            "}";

}
