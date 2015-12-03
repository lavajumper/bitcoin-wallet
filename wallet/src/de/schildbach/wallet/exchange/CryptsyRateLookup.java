package de.schildbach.wallet.exchange;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import de.schildbach.wallet.ExchangeRatesProvider;
import de.schildbach.wallet.util.GenericUtils;

public class CryptsyRateLookup extends RateLookup {

    private static final String TAG = CryptsyRateLookup.class.getName();

    public CryptsyRateLookup()
    {
        super("https://api.cryptsy.com/api/v2/markets/sxc_btc");
    }

    public Map<String, ExchangeRatesProvider.ExchangeRate> getRates(ExchangeRatesProvider.ExchangeRate usdRate) {
        if(usdRate == null)
            return null;
        final BigDecimal decUsdRate = GenericUtils.fromNanoCoins(usdRate.rate, 0);
        if(getData())
        {
            // We got data from the HTTP connection
            final Map<String, ExchangeRatesProvider.ExchangeRate> rates =
                    new TreeMap<String, ExchangeRatesProvider.ExchangeRate>();
            try
            {
                JSONObject head = new JSONObject(this.data);
                head = head.getJSONObject("data").getJSONObject("last_trade");

                JSONArray resultArray = head.getJSONArray("price");
                Log.i(TAG,resultArray.toString(2));
                // Format: eg. _cpzh4: 3.673
                Pattern p = Pattern.compile("_cpzh4: ([\\d\\.]+)");
                for(int i = 0; i < resultArray.length(); ++i) {
                    String currencyCd = resultArray.getJSONObject(i).getJSONObject("title").getString("$t");
                    String rateStr = resultArray.getJSONObject(i).getJSONObject("content").getString("$t");
                    Matcher m = p.matcher(rateStr);
                    if(m.matches())
                    {
                        // Just get the good part
                        rateStr = m.group(1);
                        Log.i(TAG, "Currency: " + currencyCd);
                        Log.i(TAG, "Rate: " + rateStr);
                        Log.i(TAG, "BTC Rate: " + decUsdRate.toString());
                        BigDecimal rate = new BigDecimal(rateStr);
                        Log.i(TAG, "Converted Rate: " + rate.toString());
                        rate = decUsdRate.multiply(rate);
                        Log.i(TAG, "Final Rate: " + rate.toString());
                        if (rate.signum() > 0)
                        {
                            rates.put(currencyCd, new ExchangeRatesProvider.ExchangeRate(currencyCd,
                                    GenericUtils.toNanoCoinsRounded(rate.toString(), 0), this.url.getHost()));
                        }
                    }else{
                        Log.i(TAG,"rateStr = " + rateStr);
                    }

                }
            } catch(JSONException e) {
                Log.i(TAG, "Bad JSON response from Cryptsy API!: " + data);
                return null;
            }
            return rates;
        }
        return null;
    }

}
