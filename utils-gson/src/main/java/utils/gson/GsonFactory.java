package utils.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.util.ServiceLoader;
import lombok.val;

public abstract class GsonFactory {

    private static final Gson GSON;

    static {
        val builder = new GsonBuilder()
            .disableHtmlEscaping();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(builder::registerTypeAdapterFactory);
        GSON = builder.create();
    }

    public static Gson getGsonInstance() {
        return GSON;
    }


    private GsonFactory() {
    }

}
