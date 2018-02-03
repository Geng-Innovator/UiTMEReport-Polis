package com.example.zaimfared.uitmereport_polis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoLaporan extends AppCompatActivity implements View.OnClickListener {

    private static List<LookUp> spnTindakan, spnKursus, spnKolej, spnFakulti, listKesalahan;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static KesalahanAdapter kesalahanAdapter;
    @SuppressLint("StaticFieldLeak")
    protected static RecyclerView recyclerView;
    private static ArrayList<String> strTindakan, strFakulti, strKolej, strKursus, strKesalahan;
    private static ArrayAdapter<String> adapter, adapter2, adapter3, adapter4;
    private static List<Long> kesalahanIndex;
    @SuppressLint("StaticFieldLeak")
    private static Spinner spinnerTindakan, spinnerFakulti, spinnerKolej, spinnerKursus;
    private static String img;
    private static String laporan;
    private static String status;
    @SuppressLint("StaticFieldLeak")
    private static AppCompatActivity info;
    @SuppressLint("StaticFieldLeak")
    private static ImageView imgViewPolis;
    private static String laporanImej, laporanStatus, laporanId, laporanTarikh, laporanMasa, laporanTempat, laporanNoKenderaan, laporanNoSiri, laporanJenis, laporanPenerangan;
    private static View vl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_laporan);

        info = this;
        strTindakan = new ArrayList<>();
        strFakulti = new ArrayList<>();
        strKolej = new ArrayList<>();
        strKursus = new ArrayList<>();
        strKesalahan = new ArrayList<>();
        kesalahanIndex = new ArrayList<>();

        context = this;
        laporan = getIntent().getStringExtra("id");
        status = getIntent().getStringExtra("status");
        if (status.equalsIgnoreCase("dijadualkan")) {
            prepareMaklumBalas();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private void prepareMaklumBalas (){
        spnFakulti = new ArrayList<>();
        spnTindakan = new ArrayList<>();
        spnKolej = new ArrayList<>();
        spnKursus = new ArrayList<>();
        listKesalahan = new ArrayList<>();

        String url = getResources().getString(R.string.url_form_maklum_balas); //Url info_laporan
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest maklumRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //Get the JSON object from the server, Response will return status and data
                    JSONObject obj = new JSONObject(response);
                    //Get status from the server. 0 - Failed, 1 - Success

                    if (obj.getString("status").equalsIgnoreCase("1")) {
                        JSONObject data = obj.getJSONObject("data");
                        JSONArray tindakanList = data.getJSONArray("tindakan_list");
                        for (int i=0; i<tindakanList.length(); i++){
                            JSONObject o = tindakanList.getJSONObject(i);
                            LookUp lookUp = new LookUp(o.getInt("id"), o.getString("nama"));
                            spnTindakan.add(lookUp);
                        }
                        JSONArray kesalahanList = data.getJSONArray("kesalahan_list");
                        for (int i=0; i<kesalahanList.length(); i++){
                            JSONObject o = kesalahanList.getJSONObject(i);
                            LookUp lookUp = new LookUp(o.getInt("id"), o.getString("nama"));
                            listKesalahan.add(lookUp);
                        }
                        JSONArray fakultiList = data.getJSONArray("fakulti_list");
                        for (int i=0; i<fakultiList.length(); i++){
                            JSONObject o = fakultiList.getJSONObject(i);
                            LookUp lookUp = new LookUp(o.getInt("id"), o.getString("nama"));
                            spnFakulti.add(lookUp);
                        }
                        JSONArray kursusList = data.getJSONArray("kursus_list");
                        for (int i=0; i<kursusList.length(); i++){
                            JSONObject o = kursusList.getJSONObject(i);
                            LookUp lookUp = new LookUp(o.getInt("id"), o.getString("nama"));
                            spnKursus.add(lookUp);
                        }
                        JSONArray kolejList = data.getJSONArray("kolej_list");
                        for (int i=0; i<kolejList.length(); i++){
                            JSONObject o = kolejList.getJSONObject(i);
                            LookUp lookUp = new LookUp(o.getInt("id"), o.getString("nama"));
                            spnKolej.add(lookUp);
                        }
                        kesalahanAdapter.notifyDataSetChanged();

                        for (LookUp l : spnTindakan) {
                            strTindakan.add(l.getName());
                        }
                        for (LookUp l : spnFakulti) {
                            strFakulti.add(l.getName());
                        }
                        for (LookUp l : spnKolej) {
                            strKolej.add(l.getName());
                        }
                        for (LookUp l : spnKursus) {
                            strKursus.add(l.getName());
                        }
                        for (LookUp l : listKesalahan) {
                            strKesalahan.add(l.getName());
                        }

                        adapter.notifyDataSetChanged();
                        adapter2.notifyDataSetChanged();
                        adapter3.notifyDataSetChanged();
                        adapter4.notifyDataSetChanged();
                    }else{
                        Toast.makeText(InfoLaporan.this, "Laporan tidak dijumpai", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(InfoLaporan.this, "Laporan tidak dijumpai", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(maklumRequest);
    }

    private static void laporanPengadu(View v){
        fetchLaporan(Integer.parseInt(laporan), v);
    }

    //Fetch laporan pengadu dgn maklum balas
    private static void fetchLaporan(final int id, final View v){
        String url = context.getResources().getString(R.string.url_info_laporan); //Url info_laporan
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest laporanRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //Get the JSON object from the server, Response will return status and data
                    JSONObject obj = new JSONObject(response);
                    //Get status from the server. 0 - Failed, 1 - Success

                    if (obj.getString("status").equalsIgnoreCase("1")) {
                        JSONObject data = obj.getJSONObject("data");

                        laporanId = String.valueOf(data.getInt("id"));
                        laporanStatus = data.getString("laporan_status");
                        laporanTarikh = data.getString("laporan_tarikh");
                        laporanMasa = data.getString("laporan_masa");
                        laporanTempat = data.getString("laporan_tempat");
                        laporanNoKenderaan = data.getString("kenderaan_no");
                        laporanNoSiri = data.getString("no_siri_pelekat");
                        laporanJenis = data.getString("kenderaan_jenis");
                        laporanPenerangan = data.getString("staf_penerangan");
                        laporanImej = data.getString("staf_imej");

                        new DownloadImageTask((ImageView) v.findViewById(R.id.info_laporan_img)).execute(laporanImej);
                        ((TextView)v.findViewById(R.id.info_laporan_id)).setText(laporanId);
                        ((TextView)v.findViewById(R.id.info_laporan_status)).setText(laporanStatus);
                        ((TextView)v.findViewById(R.id.info_laporan_tarikh)).setText(laporanTarikh);
                        ((TextView)v.findViewById(R.id.info_laporan_masa)).setText(laporanMasa);
                        ((TextView)v.findViewById(R.id.info_laporan_tempat)).setText(laporanTempat);
                        ((TextView)v.findViewById(R.id.info_laporan_no_kenderaan)).setText(laporanNoKenderaan);
                        ((TextView)v.findViewById(R.id.info_laporan_no_siri_pelekat)).setText(laporanNoSiri);
                        ((TextView)v.findViewById(R.id.info_laporan_jenis_kenderaan)).setText(laporanJenis);
                        ((TextView)v.findViewById(R.id.info_laporan_penerangan_staf)).setText(laporanPenerangan);

                        if (laporanStatus.equalsIgnoreCase("dijadualkan")){
                            (v.findViewById(R.id.info_laporan_status)).setBackgroundResource(R.color.colorOrange);
                        }else if (laporanStatus.equalsIgnoreCase("dilaporkan")){
                            (v.findViewById(R.id.info_laporan_status)).setBackgroundResource(R.color.colorGreen);
                        }else{
                            (v.findViewById(R.id.info_laporan_status)).setBackgroundResource(R.color.colorRed);
                        }

                        /***************** Part untuk view laporan                            *********/
                        if (!status.equalsIgnoreCase("dijadualkan")) {
                            new DownloadImageTask((ImageView) vl.findViewById(R.id.viewLaporanImage)).execute(data.getString("polis_imej"));
                            ((TextView)vl.findViewById(R.id.viewLaporanPolisId)).setText(data.getString("polis_id"));
                            ((TextView)vl.findViewById(R.id.viewLaporanPolisPenerangan)).setText(data.getString("polis_penerangan"));
                            JSONArray kList = data.getJSONArray("kesalahan_list");
                            String kesalahan = "";
                            for (int k=0; k<kList.length(); k++){
                                kesalahan = kesalahan + (k+1) + ". " + kList.getString(k) + System.lineSeparator();
                            }
                            ((TextView)vl.findViewById(R.id.viewLaporanPolisKesalahan)).setText(kesalahan);
                        }
                    }else{
                        Toast.makeText(context, "Laporan tidak dijumpai", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Laporan tidak dijumpai", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("laporan_id", String.valueOf(id));
                return params;
            }
        };

        requestQueue.add(laporanRequest);
    }

    private static void maklumBalas(final View v){
        //Declare
        spinnerTindakan = v.findViewById(R.id.maklum_laporan_tindakan_spinner);
        spinnerFakulti = v.findViewById(R.id.maklum_laporan_fakulti_spinner);
        spinnerKolej = v.findViewById(R.id.maklum_laporan_kolej_spinner);
        spinnerKursus = v.findViewById(R.id.maklum_laporan_kursus_spinner);
        imgViewPolis = v.findViewById(R.id.maklum_laporan_img);

        //Dynamic kan kesalahan
        adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, strTindakan);
        spinnerTindakan.setAdapter(adapter);
        adapter2 = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, strFakulti);
        spinnerFakulti.setAdapter(adapter2);
        adapter3 = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, strKolej);
        spinnerKolej.setAdapter(adapter3);
        adapter4 = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, strKursus);
        spinnerKursus.setAdapter(adapter4);

        recyclerView = v.findViewById(R.id.maklum_laporan_kesalahan);
        kesalahanAdapter = new KesalahanAdapter(strKesalahan);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(kesalahanAdapter);

        (v.findViewById(R.id.maklum_laporan_penerangan)).setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {

                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
        (v.findViewById(R.id.maklum_laporan_nama_pelajar)).setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {

                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });

        v.findViewById(R.id.btnMaklumHantar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i=0; i<listKesalahan.size(); i++){
                    View row = recyclerView.getLayoutManager().findViewByPosition(i);
                    CheckBox chkBox = row.findViewById(R.id.chkBoxRowKesalahan);
                    if (chkBox.isChecked()){
                        kesalahanIndex.add(listKesalahan.get(i).getId());
                    }
                }

                hantarMaklumBalas(v);
            }
        });
    }

    private static void hantarMaklumBalas(final View v){
        String url = context.getResources().getString(R.string.url_maklum_balas); //Url info_laporan
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest maklumRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //Get the JSON object from the server, Response will return status and data
                    JSONObject obj = new JSONObject(response);
                    //Get status from the server. 0 - Failed, 1 - Success

                    if (obj.getString("status").equalsIgnoreCase("1")) {
                        JSONObject data = obj.getJSONObject("data");
                        Toast.makeText(context, "Laporan telah berjaya dimuat naik", Toast.LENGTH_SHORT).show();
                        info.finish();
                    }else{
                        Toast.makeText(context, "Laporan tidak dijumpai", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Laporan tidak dijumpai", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                JSONArray kesalahanList = new JSONArray(kesalahanIndex);

                SharedPreferences sharedPreferences = context.getSharedPreferences("pekerjaPref", Context.MODE_PRIVATE);
                String polis_id = sharedPreferences.getString("ID", "");

                Map<String, String> params = new HashMap<>();
                //Shared preferences
                String penerangan = ((EditText)v.findViewById(R.id.maklum_laporan_penerangan)).getText().toString();
                String pelajar_no = ((EditText)v.findViewById(R.id.maklum_laporan_no_pelajar)).getText().toString();
                String pelajar_nama = ((EditText)v.findViewById(R.id.maklum_laporan_nama_pelajar)).getText().toString();
                String status_id = "" + spnTindakan.get(spinnerTindakan.getSelectedItemPosition()).getId();
                String kursus_id = "" + spnKursus.get(spinnerKursus.getSelectedItemPosition()).getId();
                String kolej_id = "" + spnKolej.get(spinnerKolej.getSelectedItemPosition()).getId();
                String fakulti_id = "" + spnFakulti.get(spinnerFakulti.getSelectedItemPosition()).getId();

                params.put("laporan_id", laporanId);
                params.put("polis_id", polis_id);
                params.put("polis_imej", img); //From camera
                params.put("status_kenderaan_id",  status_id);
                params.put("kesalahan_list_id", kesalahanList.toString()); //hantar dalam jsonarray
                params.put("penerangan_polis", penerangan);
                params.put("pelajar_no", pelajar_no);
                params.put("pelajar_nama", pelajar_nama);
                params.put("pelajar_kursus_id", kursus_id);
                params.put("pelajar_kolej_id", kolej_id);
                params.put("pelajar_fakulti_id", fakulti_id);

                return params;
            }
        };

        requestQueue.add(maklumRequest);
    }
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }

    /******************************************** OnClickListener *********************************************/
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnMaklumKembali:
                finish();
                break;
            case R.id.maklum_laporan_img:
                captureImage();
                break;
        }
    }

    public void captureImage(){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        img = encodeToBase64(bitmap);
        imgViewPolis.setImageBitmap(bitmap);
    }


    public static String encodeToBase64(Bitmap image){
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input){
        byte[] decodeBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodeBytes, 0, decodeBytes.length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info_laporan_2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;

            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1 :
                    rootView = inflater.inflate(R.layout.fragment_pengadu, container, false);
                    laporanPengadu(rootView);
                    break;
                case 2 :

                    if (status.equalsIgnoreCase("dijadualkan")) {
                        rootView = inflater.inflate(R.layout.fragment_maklum_balas, container, false);
                        maklumBalas(rootView);
                    }else{
                        rootView = inflater.inflate(R.layout.fragment_view_laporan, container, false);
                        vl = rootView;
                    }
                    break;
                default:
                    break;
            }
            return rootView;
        }
    }
}
