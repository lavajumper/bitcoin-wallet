/*
 * Copyright 2014-2015 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.ui.send;

import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.script.Script;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.schildbach.wallet.Constants;
import de.schildbach.wallet_test.R;

import de.schildbach.wallet.data.explorers.*;

/**
 * @author Andreas Schildbach
 */
public final class RequestWalletBalanceTask {
    private final Handler backgroundHandler;
    private final Handler callbackHandler;
    private final ResultCallback resultCallback;

    private static final Logger log = LoggerFactory.getLogger(RequestWalletBalanceTask.class);

    public interface ResultCallback {
        void onResult(Set<UTXO> utxos);

        void onFail(int messageResId, Object... messageArgs);
    }

    public RequestWalletBalanceTask(final Handler backgroundHandler, final ResultCallback resultCallback) {
        this.backgroundHandler = backgroundHandler;
        this.callbackHandler = new Handler(Looper.myLooper());
        this.resultCallback = resultCallback;
    }

    public void requestWalletBalance(final AssetManager assets, final Address address) {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                Context.propagate(Constants.CONTEXT);

                // Use either cryptoid or be.sexcoin.info
                List<String> urls = new ArrayList<String>(2);
                urls.add(Constants.SXC_INSIGHT_API_URL);
                urls.add(Constants.CRYPTOID_API_URL);
                //urls.add(Constants.BLOCKCYPHER_API_URL);

                Collections.shuffle(urls, new Random(System.nanoTime()));

                HttpUrl req = null;// I hate this.

                if(urls.get(0) == Constants.SXC_INSIGHT_API_URL)
                    req = SxcInsightAPI.makeGetAddress(address.toString(), "utxo");
                if(urls.get(0) == Constants.CRYPTOID_API_URL)
                    req = CryptoidAPI.makeGetAddress(address.toString(), "utxo");

                log.warn("trying to access {}", req.toString());

                final Request.Builder request = new Request.Builder();
                request.url(req);
                //request.url(HttpUrl.parse(url.toString()).newBuilder().encodedQuery("unspentOnly=true&includeScript=true").build());

                log.warn("trying to request wallet balance from {}", request.toString());
                final Call call = Constants.HTTP_CLIENT.newCall(request.build());

                try {
                    final Response response = call.execute();
                    if (response.isSuccessful()) {
                        Set<UTXO> utxSet = new HashSet<>();
                        if(urls.get(0) == Constants.SXC_INSIGHT_API_URL){
                            utxSet = SxcInsightAPI.getUTXOs(response);
                        }else if(urls.get(0) == Constants.CRYPTOID_API_URL){
                            utxSet = CryptoidAPI.getUTXOs(response);
                        }
                        final Set<UTXO> utxoSet = utxSet;
                        log.info("fetched unspent outputs from {}", req.toString());
                        onResult(utxoSet);
                    } else {
                        final String responseMessage = response.message();
                        log.info("got http error '{}: {}' from {}", response.code(), responseMessage, req.toString());
                        onFail(R.string.error_http, response.code(), responseMessage);
                    }
                } catch (final JSONException x) {
                    log.info("problem parsing json from " + req.toString(), x);
                    onFail(R.string.error_parse, x.getMessage());
                } catch (final IOException x) {
                    log.info("problem querying unspent outputs from " + req.toString(), x);
                    onFail(R.string.error_io, x.getMessage());
                }
                /*
                try {
                    final Response response = call.execute();
                    if (response.isSuccessful()) {
                        String content = response.body().string();
                        final JSONObject json = new JSONObject(content);
                        final JSONArray jsonOutputs = json.optJSONArray("txrefs");

                        final Set<UTXO> utxoSet = new HashSet<>();
                        if (jsonOutputs == null) {
                            onResult(utxoSet);
                            return;
                        }

                        for (int i = 0; i < jsonOutputs.length(); i++) {
                            final JSONObject jsonOutput = jsonOutputs.getJSONObject(i);

                            final Sha256Hash utxoHash = Sha256Hash.wrap(jsonOutput.getString("tx_hash"));
                            final int utxoIndex = jsonOutput.getInt("tx_output_n");
                            final byte[] utxoScriptBytes = Hex.decode(jsonOutput.getString("script"));
                            final Coin uxtutx = Coin.valueOf(Long.parseLong(jsonOutput.getString("value")));

                            UTXO utxo = new UTXO(utxoHash, utxoIndex, uxtutx, -1, false, new Script(utxoScriptBytes));
                            utxoSet.add(utxo);
                        }

                        log.info("fetched unspent outputs from {}", url);
                        onResult(utxoSet);
                    } else {
                        final String responseMessage = response.message();
                        log.info("got http error '{}: {}' from {}", response.code(), responseMessage, url);
                        onFail(R.string.error_http, response.code(), responseMessage);
                    }
                } catch (final JSONException x) {
                    log.info("problem parsing json from " + url, x);
                    onFail(R.string.error_parse, x.getMessage());
                } catch (final IOException x) {
                    log.info("problem querying unspent outputs from " + url, x);
                    onFail(R.string.error_io, x.getMessage());
                }
                */

            }
        });
    }

    protected void onResult(final Set<UTXO> utxos) {
        callbackHandler.post(new Runnable() {
            @Override
            public void run() {
                resultCallback.onResult(utxos);
            }
        });
    }

    protected void onFail(final int messageResId, final Object... messageArgs) {
        callbackHandler.post(new Runnable() {
            @Override
            public void run() {
                resultCallback.onFail(messageResId, messageArgs);
            }
        });
    }
}
