package com.mojahid2021.shortly;

import static android.view.View.VISIBLE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment {
    private EditText urlInputText;
    private MaterialButton btnSubmit, btnCopy, btnShare;
    private TextView tvURL, tvOriginalURL;
    private ProgressBar progressBar;
    private MaterialCardView resultCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        urlInputText = view.findViewById(R.id.etURL);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        tvURL = view.findViewById(R.id.tvURL);
        btnCopy = view.findViewById(R.id.btnCopy);
        progressBar = view.findViewById(R.id.progressBar);
        btnShare = view.findViewById(R.id.btnShare);
        resultCard = view.findViewById(R.id.resultCard);
        tvOriginalURL = view.findViewById(R.id.tvOriginalURL);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = urlInputText.getText().toString();
                if (url.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter url...", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(VISIBLE);
                    getShortUrl(url);
                }
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Shortened URL", tvURL.getText().toString());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), "URL copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlToShare = tvURL.getText().toString();
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_SUBJECT, "Check this out");
                share.putExtra(Intent.EXTRA_TEXT, urlToShare);
                startActivity(Intent.createChooser(share, "Share via"));
            }
        });

        return view;
    }

    private void getShortUrl(String url) {
        Map<String, String> shortenRequest = new HashMap<>();
        shortenRequest.put("alias", "");
        shortenRequest.put("original_url", url);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ShortenResponse> call = apiService.shortenUrl(shortenRequest);
        call.enqueue(new Callback<ShortenResponse>() {
            @Override
            public void onResponse(Call<ShortenResponse> call, Response<ShortenResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    resultCard.setVisibility(VISIBLE);
                    ShortenResponse shortenResponse = response.body();
                    if (shortenResponse != null) {
                        String shortUrl = shortenResponse.getShortUrl();
                        String originalUrl = shortenResponse.getOriginalUrl();
                        Toast.makeText(getContext(), "Shortened URL: " + shortUrl, Toast.LENGTH_LONG).show();
                        Toast.makeText(getContext(), "Original URL: " + originalUrl, Toast.LENGTH_LONG).show();
                        tvOriginalURL.setText(originalUrl);
                        tvURL.setText(shortUrl);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to shorten URL", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ShortenResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
