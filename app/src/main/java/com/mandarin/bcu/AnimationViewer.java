package com.mandarin.bcu;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.mandarin.bcu.androidutil.FilterUnit;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.asynchs.Adapters;
import com.mandarin.bcu.androidutil.asynchs.Adder;
import com.mandarin.bcu.util.system.android.BMBuilder;
import com.mandarin.bcu.util.system.fake.ImageBuilder;
import com.mandarin.bcu.util.unit.Form;

import java.io.File;
import java.util.ArrayList;

public class AnimationViewer extends AppCompatActivity {

    protected ImageButton search;
    private ListView list;
    private int unitnumber;
    static final int REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shared = getSharedPreferences("configuration",MODE_PRIVATE);
        SharedPreferences.Editor ed;
        if(!shared.contains("initial")) {
            ed = shared.edit();
            ed.putBoolean("initial",true);
            ed.putBoolean("theme",true);
            ed.apply();
        } else {
            if(!shared.getBoolean("theme",false)) {
                setTheme(R.style.AppTheme_night);
            } else {
                setTheme(R.style.AppTheme_day);
            }
        }

        setContentView(R.layout.activity_animation_viewer);

        ImageBuilder.builder = new BMBuilder();

        String unitpath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/unit/";

        File f = new File(unitpath);
        unitnumber = f.listFiles().length;

        ImageButton back = findViewById(R.id.animbck);
        search = findViewById(R.id.animsch);
        list = findViewById(R.id.unitinflist);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoFilter();
            }
        });

        new Adder(unitnumber,this).execute();
    }

    protected String showName(int location) {
        ArrayList<String> names = new ArrayList<>();

        for(Form f : StaticStore.units.get(location).forms) {
            names.add(f.name);
        }

        StringBuilder result = new StringBuilder(withID(location, names.get(0)));

        for(int i = 1; i < names.size();i++) {
            result.append(" - ").append(names.get(i));
        }

        return result.toString();
    }

    protected String withID(int id, String name) {
        String result;

        if(name.equals("")) {
            result = number(id);
        } else {
            result = number(id)+" - "+name;
        }

        return result;
    }

    protected void gotoFilter() {
        Intent intent = new Intent(this, SearchFilter.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    protected String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ArrayList<String> rarity;
        ArrayList<String> attack;
        ArrayList<String> target;
        ArrayList<ArrayList<Integer>> ability;
        boolean atksimu;
        boolean atkorand;
        boolean tgorand;
        boolean aborand;
        boolean empty;

        if(resultCode == RESULT_OK) {
            assert data != null;
            Bundle extra = data.getExtras();

            assert extra != null;
            empty = extra.getBoolean("empty");
            rarity = extra.getStringArrayList("rare");
            attack = extra.getStringArrayList("attack");
            target = extra.getStringArrayList("target");
            ability = (ArrayList<ArrayList<Integer>>) extra.getSerializable("ability");
            System.out.println(ability);
            atksimu = extra.getBoolean("atksimu");
            atkorand = extra.getBoolean("atkorand");
            tgorand = extra.getBoolean("tgorand");
            aborand = extra.getBoolean("aborand");

            FilterUnit filterUnit = new FilterUnit(rarity,attack,target,ability,atksimu,atkorand,tgorand,aborand,empty,unitnumber);
            ArrayList<Integer> newNumber = filterUnit.setFilter();
            ArrayList<String> newName = new ArrayList<>();

            for(int i : newNumber) {
                newName.add(StaticStore.names[i]);
            }

            Adapters adapters = new Adapters(this,newName.toArray(new String[0]),StaticStore.bitmaps,newNumber);
            list.setAdapter(adapters);
            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(AnimationViewer.this,showName(newNumber.get(position)),Toast.LENGTH_SHORT).show();

                    return false;
                }
            });
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent result = new Intent(AnimationViewer.this,UnitInfo.class);
                    result.putExtra("ID",newNumber.get(position));
                    startActivity(result);
                }
            });

            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AnimationViewer.this,SearchFilter.class);

                    intent.putExtra("empty",empty);
                    intent.putExtra("tgorand",tgorand);
                    intent.putExtra("atksimu",atksimu);
                    intent.putExtra("aborand",aborand);
                    intent.putExtra("atkorand",atkorand);
                    intent.putExtra("target",target);
                    intent.putExtra("attack",attack);
                    intent.putExtra("rare",rarity);
                    intent.putExtra("ability",ability);
                    setResult(Activity.RESULT_OK,intent);
                    startActivityForResult(intent,REQUEST_CODE);
                }
            });
        }
    }
}
