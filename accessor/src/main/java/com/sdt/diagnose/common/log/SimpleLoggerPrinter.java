package com.sdt.diagnose.common.log;

import static com.orhanobut.logger.Logger.ASSERT;
import static com.orhanobut.logger.Logger.DEBUG;
import static com.orhanobut.logger.Logger.ERROR;
import static com.orhanobut.logger.Logger.INFO;
import static com.orhanobut.logger.Logger.VERBOSE;
import static com.orhanobut.logger.Logger.WARN;
import static com.sdt.diagnose.common.log.Utils.checkNotNull;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.LogAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class SimpleLoggerPrinter implements PrinterProxy {

    /**
     * It is used for json pretty print
     */
    private static final int JSON_INDENT = 2;

    /**
     * Provides one-time used tag for the log message
     */
    private final ThreadLocal<String> localTag = new ThreadLocal<>();

    private final List<LogAdapter> logAdapters = new ArrayList<>();

//    private final Set<String> blackListClasses = new HashSet<>();

    @Override
    public PrinterProxy t(String tag) {
        if (tag != null) {
            localTag.set(tag);
        }
        return this;
    }

    @Override
    public void d(@NonNull String message, @Nullable Object... args) {
        log(DEBUG, null, message, args);
    }

    @Override
    public void d(@Nullable Object object) {
        log(DEBUG, null, Utils.toString(object));
    }

    @Override
    public void e(@NonNull String message, @Nullable Object... args) {
        e(null, message, args);
    }

    @Override
    public void e(
            @Nullable Throwable throwable, @NonNull String message, @Nullable Object... args) {
        log(ERROR, throwable, message, args);
    }

    @Override
    public void w(@NonNull String message, @Nullable Object... args) {
        log(WARN, null, message, args);
    }

    @Override
    public void i(@NonNull String message, @Nullable Object... args) {
        log(INFO, null, message, args);
    }

    @Override
    public void v(@NonNull String message, @Nullable Object... args) {
        log(VERBOSE, null, message, args);
    }

    @Override
    public void wtf(@NonNull String message, @Nullable Object... args) {
        log(ASSERT, null, message, args);
    }

    @Override
    public void json(@Nullable String json) {
        if (Utils.isEmpty(json)) {
            d("Empty/Null json content");
            return;
        }
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.toString(JSON_INDENT);
                d(message);
                return;
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(JSON_INDENT);
                d(message);
                return;
            }
            e("Invalid Json");
        } catch (JSONException e) {
            e("Invalid Json");
        }
    }

    @Override
    public void xml(@Nullable String xml) {
        if (Utils.isEmpty(xml)) {
            d("Empty/Null xml content");
            return;
        }
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            d(xmlOutput.getWriter().toString().replaceFirst(">", ">\n"));
        } catch (TransformerException e) {
            e("Invalid xml");
        }
    }

    @Override
    public synchronized void log(
            int priority,
            @Nullable String tag,
            @Nullable String message,
            @Nullable Throwable throwable) {
        boolean isLoggable = false;
        for (LogAdapter adapter : logAdapters) {
            if (adapter.isLoggable(priority, tag)) {
                isLoggable = true;
            }
        }
        if (!isLoggable) {
            return;
        }

        if (throwable != null && message != null) {
            message += " : " + Utils.getStackTraceString(throwable);
        }
        if (throwable != null && message == null) {
            message = Utils.getStackTraceString(throwable);
        }
        if (Utils.isEmpty(message)) {
            message = "Empty/NULL log message";
        }
        Throwable stackThrowable1 = new Throwable();
        StackTraceElement[] traceElements = stackThrowable1.getStackTrace();
        int stackIndex = Utils.getExternalCallerStackTrace(LogUtils.class.getName(), traceElements);
        if (stackIndex < 0) {
            stackIndex =
                    Utils.getExternalCallerStackTrace(
                            SimpleLoggerPrinter.class.getName(), traceElements);
        }
        StackTraceElement traceElement = traceElements[stackIndex];
        message =
                String.format(
                        "%s(%s:%d)",
                        TextUtils.isEmpty(message) ? "" : message,
                        traceElement.getFileName(),
                        traceElement.getLineNumber());
        for (LogAdapter adapter : logAdapters) {
            if (adapter.isLoggable(priority, tag)
//                    && !blackListClasses.contains(traceElement.getClassName())
            ) {
                adapter.log(priority, tag, message);
            }
        }
    }

//    void addBlackList(Class aClass) {
//        blackListClasses.add(aClass.getName());
//    }
//
//    void removeBlackList(Class aClass) {
//        blackListClasses.remove(aClass.getName());
//    }

    @Override
    public void clearLogAdapters() {
        logAdapters.clear();
    }

    @Override
    public void addAdapter(@NonNull LogAdapter adapter) {
        logAdapters.add(checkNotNull(adapter));
    }

    /**
     * This method is synchronized in order to avoid messy of logs' order.
     */
    private synchronized void log(
            int priority,
            @Nullable Throwable throwable,
            @NonNull String msg,
            @Nullable Object... args) {
        checkNotNull(msg);
        if (!LogUtils.isDebug() && priority < ERROR) {
            return;
        }
        String tag = getTag();
        String message = createMessage(msg, args);
        log(priority, tag, message, throwable);
    }

    /**
     * @return the appropriate tag based on local or global
     */
    @Nullable
    private String getTag() {
        String tag = localTag.get();
        if (tag != null) {
            localTag.remove();
            return tag;
        }
        return null;
    }

    @NonNull
    private String createMessage(@NonNull String message, @Nullable Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null || !args[i].getClass().isArray()) {
                continue;
            }
            args[i] = Utils.toString(args[i]);
        }
        return args.length == 0 ? message : String.format(message, args);
    }
}
