/*
 * Copyright 2011-2019 the original author or authors.
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

package de.schildbach.wallet.data.explorers;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import de.schildbach.wallet.Constants;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.script.Script;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SxcInsightAPI {
    private static HttpUrl loc = HttpUrl.parse(Constants.SXC_INSIGHT_API_URL);

    public static String decode(Response resp){
        return(new String("Method not Implemented."));
    };

    public static HttpUrl makeGetAddress(String address, String method){
        loc=HttpUrl.parse(Constants.SXC_INSIGHT_API_URL);
        if(!method.isEmpty()) {
            loc = loc.newBuilder()
                    .addPathSegment("addr")
                    .addPathSegment(address)
                    .addPathSegment(method).build();

        }else{
            loc = loc.newBuilder()
                    .addPathSegment("addr")
                    .addPathSegment(address)
                    .build();
        }
        return loc;
    }

    public static HttpUrl makeGetBlock(String hash) {
        loc=HttpUrl.parse(Constants.SXC_INSIGHT_API_URL);
        loc = loc.newBuilder()
                .addPathSegment("block")
                .addPathSegment(hash)
                .build();
        return loc;
    }

    public static HttpUrl makeGetBlock(int height) {
        loc=HttpUrl.parse(Constants.SXC_INSIGHT_API_URL);
        loc = loc.newBuilder()
                .addPathSegment("block-index")
                .addPathSegment(Integer.toString(height))
                .build();
        return loc;
    }

    public static HttpUrl makeGetTransaction(String hash) {
        loc=HttpUrl.parse(Constants.SXC_INSIGHT_API_URL);
        loc = loc.newBuilder()
                .addPathSegment("tx")
                .addPathSegment(hash)
                .build();
        return loc;
    }

    public static Set<UTXO> getUTXOs(Response response) throws JSONException, IOException {
        final Set<UTXO> utxoSet = new HashSet<>();

        String content = response.body().string();
        //final JSONObject json = new JSONObject(content);
        final JSONArray jsonOutputs = new JSONArray(content);

        if (jsonOutputs == null) {
            return (utxoSet);
        }

        for (int i = 0; i < jsonOutputs.length(); i++) {
            final JSONObject jsonOutput = jsonOutputs.getJSONObject(i);


            final Sha256Hash utxoHash = Sha256Hash.wrap(jsonOutput.getString("txid"));
            final int utxoIndex = jsonOutput.getInt("vout");
            final byte[] utxoScriptBytes = Hex.decode(jsonOutput.getString("scriptPubKey"));
            final Coin uxtutx = Coin.valueOf(Long.parseLong(jsonOutput.getString("satoshis")));
            final int height = jsonOutput.getInt("height");

            UTXO utxo = new UTXO(utxoHash, utxoIndex, uxtutx, height, false, new Script(utxoScriptBytes));
            utxoSet.add(utxo);
        }
        return utxoSet;
    }

    public static HttpUrl getBaseUrl(){ return loc = HttpUrl.parse(Constants.SXC_INSIGHT_API_URL); }

}
