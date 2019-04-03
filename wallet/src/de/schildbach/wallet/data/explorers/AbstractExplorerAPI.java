package de.schildbach.wallet.data.explorers;

import com.squareup.okhttp.HttpUrl;

abstract class AbstractExplorerAPI {

    abstract public HttpUrl makeGetAddress(String address, String method);
    abstract public HttpUrl makeGetBlock(String hash);
    abstract public HttpUrl makeGetBlock(int height);
    abstract public HttpUrl makeGetTransaction(String hash);

}
