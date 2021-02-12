package utils.okhttp;

public class ConnectionCloseHeaderInterceptor extends AbstractHeaderInterceptor {

    public ConnectionCloseHeaderInterceptor() {
        super("Connection");
    }

    @Override
    public String getValue() {
        return "close";
    }

}
