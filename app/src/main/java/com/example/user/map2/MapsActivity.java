package com.example.user.map2;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import static android.R.attr.category;
import static android.R.attr.order;
import static com.example.user.map2.R.attr.title;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    public static final String retrieveP = "RETRIEVE_POI";
    public static final String retrieveAP = "RETRIEVE_ALL_POIs";
    public static final String parseDocPoi = "PARSE_DOC_POI";
    final String dbname = "myapp";
    private String docId = "123";
    private Manager manager;
    private Database couchDb;
    Poi poi;
    String JSONString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.i("Status: ", "Start Couchbase App");
        if (!Manager.isValidDatabaseName(dbname)) {
            Log.i("Status: ", "Bad couchbase db name!");
            return;
        }
        createManager();
        createCouchdb();
        createDocument(docId);

        Document retrievedDocument = retrieveDocument(docId);
        updateDocument(retrievedDocument);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //deleteDocument(retrievedDocument);

    }

    public void createManager() {
        try {
            manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
            Log.i("createManager", "Couchbase Manager created!");
        } catch (IOException e) {
            Log.i("createManager", "Failed to create Couchbase Manager!");
            return;
        }
    }

    public void createCouchdb() {
        try {
            couchDb = manager.getDatabase(dbname);
            Log.i("createCouchdb", "Couchbase Database created!");
        } catch (CouchbaseLiteException e) {
            Log.i("createCouchdb", "Failed to create Couchbase Database!");
            return;
        }
    }

    public void createDocument(String docId) {
        // create some dummy data
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());
        Poi poi = new Poi("Marker",120.0,120.0, "Marker Category", "Marker Order");

        // put those dummy data together
        Map<String, Object> docContent = new HashMap<String, Object>();
        docContent.put("title", poi.getTitle());
        docContent.put("longitude", poi.getLongitude());
        docContent.put("latitude", poi.getLatitude());
        docContent.put("category", poi.getCategory());
        docContent.put("order", poi.getOrder());

        //docContent.put("poi", poi);
        Log.i("createDocument", "docContent=" + String.valueOf(docContent));


        // create an empty document, add content and write it to the couchDb
        Document document = new Document(couchDb, docId);
        try {
            document.putProperties(docContent);
            Log.i("createDocument: ", "Document written to couchDb named " + dbname + " with ID = " + document.getId());

        } catch (CouchbaseLiteException e) {
            Log.i("createDocument: ", "Failed to write document to Couchbase database!");

        }
    }

    public Document retrieveDocument(String docId) {
        Document retrievedDocument = couchDb.getDocument(docId);
        Log.i("retrieveDocument", "retrievedDocument Properties=" + String.valueOf(retrievedDocument.getProperties()));
        return retrievedDocument;
    }

    public void updateDocument(Document doc) {
        Map<String, Object> updatedProperties = new HashMap<String, Object>();
        updatedProperties.putAll(doc.getProperties());
        //updatedProperties.put ("message", "We're having a heat wave!");
        //updatedProperties.put ("temperature", "95");

        try {
            doc.putProperties(updatedProperties);
            Log.i("updateDocument", "updated retrievedDocument=" + String.valueOf(doc.getProperties()));
        } catch (CouchbaseLiteException e) {
            Log.i("updateDocument", "Failed to update document!");

        }
//        Object Latitude = doc.getProperties().get("latitude");
//        Log.i("updateDocument - Latitude:", Latitude.toString());
//        Object Longitude = doc.getProperties().get("longitude");
//        Log.i("updateDocument - Longitude:", Longitude.toString());

    }

    public void deleteDocument(Document doc) {
        try {
            doc.delete();
            Log.i("deleteDocument: ", "Document deleted from Couchbase database!");
        } catch (CouchbaseLiteException e) {
            Log.i("deleteDocument: ", "Failed to write delete document from Couchbase database!");
        }
    }

    private Poi retrievePoi(Database database, String docId) {

        Object poiObj  = null;
        Document retrievedDocument = database.getDocument(docId);
        Log.i( retrieveP + " - ID:", retrievedDocument.getId()); //empty!!!

        Poi p1 = null;
        try{
            p1 = parseDocPoi(retrievedDocument);
        }
        catch (Exception e){
            Log.i("exception",e.getMessage());
        }
        Log.d(retrieveP , poiObj.toString());
        //Retrieve the document by id
        return p1;
    }

    //from retrievePoi
    private Poi parseDocPoi(Document d ) {

        Object poiObj;
        Gson gson = new Gson();
        Map<String, Object> properties = d.getProperties();

        String Title = (String) properties.get("title");
        //String Title = d.getProperty("title").toString();
        String Category = (String) properties.get("category");
        String Order = (String) properties.get("order");
//        String Latitude = properties.get("latitude").toString();
//        String Longitude = properties.get("longitude").toString();

//        String Order = d.getProperty("order").toString();
//        String Latitude = d.getProperty("latitude").toString();
//        String Longitude  = d.getProperty("longitude").toString();

        Double mLatitude = (Double) properties.get("latitude");
        Double mLongitude = (Double) properties.get("longitude");


//        Log.i(parseDocPoi + " title: ", Title);
//        Log.i(parseDocPoi + " Category: ", Category);
//        Log.i(parseDocPoi + " Longitude: ", Longitude);
//        Log.i(parseDocPoi + " Latitude: ", Latitude);
//        Log.i(parseDocPoi + " Order: ", Order);


        Poi mPoi = new Poi(Title, mLatitude, mLongitude, Category, Order);
        //String JSONString = gson.toJson(poiObj, Poi.class); //Convert the object to json string using Gson
        //Poi poi = (Poi) poiObj;
        gson.fromJson(JSONString, Poi.class); //convert the json string to Poi object
        //Log.i(parseDocPoi + " - JSONString:", JSONString); //empty!!!
        Log.i(parseDocPoi + " getPoiFromDocument ", "jsonString>>>" + mPoi.getCategory()); //Marker Category
        return mPoi;
    }

    //from OnMapReady
    private ArrayList<Poi> retrieveAllPois(Database database) {

        ArrayList<Poi> listOfPois = new ArrayList<Poi>();

        // Let's find the documents that have conflicts so we can resolve them:
        Query query = database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {

            QueryRow row = it.next();
            Log.i(retrieveAP, "Query Document ID is: ", row); //empty
            Log.i(retrieveAP, "Query result is: ", row.getDocumentId()); //empty

            //p = parseDocPoi(row.getDocument());
            Log.i( retrieveAP + " - ID:", row.getDocument().getId()); //=78870b85-747d-42c1-ae07-4b0dd96d5fdb
            Document doc;
            doc = retrieveDocument(row.getDocument().getId());
            Log.i(retrieveAP + " - Id:", doc.getProperties().get("_id").toString());
            Log.i(retrieveAP + " - Rev:", doc.getProperties().get("_rev").toString());

//            ObjectMapper mapper = new ObjectMapper();  // Usually you only need one instance of this per app.
//            try {
//                JSONString = retrieveDocument(docId).toString();
//                Map<String, Object> map = mapper.readValue(JSONString, Map.class);
//                Document document = couchDb.getDocument(docId);
//                document.putProperties(map);
//            } catch (IOException | CouchbaseLiteException ex) {
//                ex.printStackTrace();
//            }

            Poi p = parseDocPoi(row.getDocument());
//            Log.i(retrieveAP + " 1 - Category ", p.getCategory());
//            Log.i(retrieveAP + " 1 - Title ", p.getTitle());
//            Log.i(retrieveAP + " 1 - Order ", p.getOrder());
//            Log.i(retrieveAP + " 1 - Latitude ", Double.toString(p.getLatitude()));
//            Log.i(retrieveAP + " 1 - Longitude ", Double.toString(p.getLongitude()));

            // Adds a poi to the pois array list.
            Poi PoiObjtect = new Poi(p.getTitle(), 120.00, 120.00,p.getCategory(),p.getOrder() ); // Creating a new object
            listOfPois.add(PoiObjtect); // Adding it to the list

            int listSize = listOfPois.size();

            for (int i = 0; i<listSize; i++){
//                Log.i("Member name: ", listOfPois.get(i).getTitle());
            }
//            listOfPois.add(p);
//            Log.i(retrieveAP + "2 - categ", p.getCategory());
//            listOfPois.add(p);
        }

        return listOfPois;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //String total2 = String.valueOf( poi.getLatitude());
        ArrayList<Poi> pois = retrieveAllPois(couchDb);
//            Log.i("+++++++++++++++++getLatitude::::::::::::::",pois.get(0).toString());
            Poi p;
            p = retrievePoi(couchDb,"");
            p.setLatitude(poi.getLatitude());
            p.setLongitude(poi.getLongitude());


        // Add a marker in Sydney, Australia, and move the camera.
            //LatLng sydney = new LatLng(-34, 151);
            LatLng marker = new LatLng(pois.get(0).getLatitude(), pois.get(0).getLongitude());

            //LatLng marker = new LatLng(p.getLatitude(), p.getLongitude());
            mMap.addMarker(new MarkerOptions().position(marker).title(p.getTitle()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));

        Log.i("Status: ", "End the App!");
    }

}