package fr.testappli.googlemapapi.fragments;

import fr.testappli.googlemapapi.notifications.MyResponse;
import fr.testappli.googlemapapi.notifications.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAABw-kpIw:APA91bEbhHPWMz7zvWROx1AxarVmICJ9H6t3DXRTd7GEzOnZoZRdDICgQ8LMyuLPLHvjsYYzNWGywK2KdioQxpb5KqyECCdNOJscP9uvIwSExrWbftgjV0WCTaO09Zi-oGmB0MHtRfsn"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
