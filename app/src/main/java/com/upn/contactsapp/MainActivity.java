package com.upn.contactsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.upn.contactsapp.activities.CreateContactActivity;
import com.upn.contactsapp.activities.LoginActivity;
import com.upn.contactsapp.adapters.ContactAdaptar;
import com.upn.contactsapp.daos.ContactDAO;
import com.upn.contactsapp.entities.Contact;
import com.upn.contactsapp.services.ContactService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final int CONTACTS_PER_PAGE = 10;
    private List<Contact> elementos = new ArrayList<>();
    private ContactAdaptar adaptar;
    private int currentPage = 1;
    private boolean isLoading = false;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews(); // Inicializa las vistas
        setUpRecyclerView();
        loadContacts(currentPage);
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
    }

    private void setUpRecyclerView() {
        RecyclerView rvContacts = findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        adaptar = new ContactAdaptar(elementos);
        rvContacts.setAdapter(adaptar);

        rvContacts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && !isLoading) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) rvContacts.getLayoutManager();
                    if (layoutManager != null && (layoutManager.getChildCount() + layoutManager.findFirstVisibleItemPosition()) >= layoutManager.getItemCount()) {
                        currentPage++; // Avanza a la siguiente página
                        loadContacts(currentPage); // Carga más contactos
                    }
                }
            }
        });
    }

    private void loadContacts(int page) {
        isLoading = true; // Marca como cargando
        progressBar.setVisibility(View.VISIBLE); // Muestra el ProgressBar

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://66d5b903f5859a7042673752.mockapi.io")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ContactService service = retrofit.create(ContactService.class);

        service.getAll(CONTACTS_PER_PAGE, page).enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                handleResponse(response);
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable throwable) {
                handleError(throwable);
            }
        });
    }

    private void handleResponse(Response<List<Contact>> response) {
        isLoading = false; // Marca como no cargando
        progressBar.setVisibility(View.GONE); // Oculta el ProgressBar

        if (response.isSuccessful() && response.body() != null) {
            List<Contact> newContacts = response.body();
            elementos.addAll(newContacts);
            adaptar.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Error al cargar contactos: " + response.message(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleError(Throwable throwable) {
        isLoading = false; // Marca como no cargando
        progressBar.setVisibility(View.GONE); // Oculta el ProgressBar
        Log.e("MAIN_APP", "Error al cargar contactos: " + throwable.getMessage());
        Toast.makeText(this, "Error al cargar contactos, por favor intenta de nuevo.", Toast.LENGTH_SHORT).show();
    }
}
