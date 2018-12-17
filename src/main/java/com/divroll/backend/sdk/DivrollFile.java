package com.divroll.backend.sdk;

import com.divroll.http.client.HttpRequestException;
import com.divroll.http.client.exceptions.BadRequestException;
import com.divroll.http.client.exceptions.UnauthorizedRequestException;
import com.google.gwt.http.client.URL;
import elemental2.core.ArrayBuffer;
import elemental2.core.Int8Array;
import elemental2.dom.File;
import elemental2.dom.FormData;
import elemental2.dom.XMLHttpRequest;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import jsinterop.base.Js;

public class DivrollFile {

    private File file;
    private String path;

    public DivrollFile(File file, String path) {
        this.file = file;
        this.path = path;
    }

    private DivrollFile() {}

    public Observable<Double> upload() {
        return Observable.create(e -> {
            FormData formData = new FormData();
            formData.append("file", file);
            XMLHttpRequest request = new XMLHttpRequest();
            request.upload.onprogress = p0 -> {
                double percentage = 100 - ( ( (p0.total - p0.loaded) / p0.total) * 100 );
                if(!Double.isInfinite(percentage)) {
                    e.onNext(percentage);
                }
            };

            request.onerror = p0 -> {
                Throwable throwable = Js.cast(p0);
                e.onError(throwable);
                return null;
            };

            request.onreadystatechange = p0 -> {
                if(request.readyState == XMLHttpRequest.DONE) {
                    if(request.status == 401) {
                        e.onError(new UnauthorizedRequestException());
                    } else if(request.status >= 400) {
                        e.onError(new BadRequestException());
                    } else if(request.status == 200) {
                        // Success
                    }
                }
                return null;
            };

            String completeUrl = Divroll.getServerUrl() + "/files/" + Divroll.getAppId() + "/" + URL.encode(path);
            request.open("POST", URL.encode(completeUrl));
            request.setRequestHeader("X-Divroll-Auth-Token", Divroll.getAuthToken());
            request.setRequestHeader("X-Divroll-App-Id", Divroll.getAppId());

            request.send(formData);
        });
    }

    public Observable<byte[]> download() {
        return Observable.create(e -> {
            XMLHttpRequest request = new XMLHttpRequest();
            request.responseType = "arraybuffer";
            //XHR binary charset opt by Marcus Granado 2006 [http://mgran.blogspot.com]
            request.overrideMimeType("text\\/plain; charset=x-user-defined");
            request.onload = p0 -> {
                int[] intArray = Js.uncheckedCast(new Int8Array(Js.<ArrayBuffer>cast(request.response)));
                byte[] byteArray = new byte[intArray.length];
                for (int j = 0; j < intArray.length; j++) byteArray[j] = (byte) intArray[j];
                e.onNext(byteArray);
            };

            request.upload.onprogress = p0 -> {
                double percentage = 100 - ( ( (p0.total - p0.loaded) / p0.total) * 100 );
                if(!Double.isInfinite(percentage)) {
                    // emmit this
                }
            };

            request.onerror = p0 -> {
                // TODO - find a way to handle 'p0' for this method:
                e.onError(new HttpRequestException());
                return null;
            };

            request.onreadystatechange = p0 -> {
                if(request.readyState == XMLHttpRequest.DONE) {
                    if(request.status == 401) {
                        e.onError(new UnauthorizedRequestException());
                    } else if(request.status >= 400) {
                        e.onError(new BadRequestException());
                    } else if(request.status == 200) {
                        // Success
                    }
                }
                return null;
            };

            String completeUrl = Divroll.getServerUrl() + "/files/" + Divroll.getAppId() + "/" + URL.encode(path);
            request.open("GET", URL.encode(completeUrl));
            request.send();
        });
    }

}
