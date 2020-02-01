package amidst.remote.shm;

public class Constants {

    public static int REQUEST_AREA_START = 0;
    public static int REQUEST_AREA_SIZE = 2 * 1024;
    public static int REQUEST_AREA_HEADER_SIZE = 64;
    public static int REQUEST_AREA_DATA_START = REQUEST_AREA_START + REQUEST_AREA_HEADER_SIZE;
    public static int REQUEST_AREA_DATA_SIZE = REQUEST_AREA_SIZE - REQUEST_AREA_DATA_START;

    public static int RESPONSE_AREA_START = REQUEST_AREA_START + REQUEST_AREA_SIZE;
    // most data requested for biomedatareply is 128*128 == 16k ints
    // biomelist responses can theoretically larger
    public static int RESPONSE_AREA_SIZE = 128 * 128 * 8 + 2048;
    public static int RESPONSE_AREA_HEADER_SIZE = 64;
    public static int RESPONSE_AREA_DATA_START = RESPONSE_AREA_START + RESPONSE_AREA_HEADER_SIZE;
    public static int RESPONSE_AREA_DATA_SIZE = RESPONSE_AREA_SIZE - RESPONSE_AREA_DATA_START;

    public static int TOTAL_BUFFER_SIZE = REQUEST_AREA_SIZE + RESPONSE_AREA_SIZE;

}
