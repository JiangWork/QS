package tool;

import java.io.IOException;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JobHistoryReaderViaRest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url("https://apollo-phx-jt.vip.ebay.com:51111/ws/v1/history").build();
		
		Response response = client.newCall(request).execute();
		if(response.isSuccessful()) {
			String json = response.body().string();
			System.out.println("OK: " + json);
			JSONObject jsonObj = new JSONObject(json);
			
		} else {
			System.out.println("ERROR:" + response.message());
		}
		
	}

}
