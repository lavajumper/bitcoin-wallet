package de.schildbach.wallet.data.explorers;

import com.squareup.okhttp.HttpUrl;

/**
 * This is NEVER USED! This has been placed in the project as a template for Explorer APIs.
 * TODO: Get this done correctly and available for java < 8
 */
abstract class AbstractExplorerAPI {

    abstract public HttpUrl makeGetAddress(String address, String method);
    abstract public HttpUrl makeGetBlock(String hash);
    abstract public HttpUrl makeGetBlock(int height);
    abstract public HttpUrl makeGetTransaction(String hash);

}
