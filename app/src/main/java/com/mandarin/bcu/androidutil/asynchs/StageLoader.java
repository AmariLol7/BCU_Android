package com.mandarin.bcu.androidutil.asynchs;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.adapters.StageListAdapter;

import java.lang.ref.WeakReference;

import common.system.MultiLangCont;
import common.util.stage.MapColc;
import common.util.stage.StageMap;

public class StageLoader extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;
    private final int mapcode;
    private final int stid;

    public StageLoader(Activity activity, int mapcode, int stid) {
        this.weakReference = new WeakReference<>(activity);
        this.mapcode = mapcode;
        this.stid = stid;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ListView stglist = activity.findViewById(R.id.stglist);

        stglist.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if(activity == null) return null;

        MapColc mc = StaticStore.map.get(mapcode);

        if(mc == null) return null;

        StageMap stm = mc.maps[stid];

        String [] stages = new String[stm.list.size()];

        for(int i = 0; i < stages.length; i++) {
            stages[i] = MultiLangCont.STNAME.getCont(stm.list.get(i));
        }

        StageListAdapter stageListAdapter = new StageListAdapter(activity,stages,mapcode,stid);
        ListView stglist = activity.findViewById(R.id.stglist);

        stglist.setAdapter(stageListAdapter);

        ImageButton bck = activity.findViewById(R.id.stglistbck);
        bck.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                activity.finish();
            }
        });

        return null;
    }

    @Override
    protected void onPostExecute(Void results) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ListView stglist = activity.findViewById(R.id.stglist);
        ProgressBar stgprog = activity.findViewById(R.id.stglistprog);
        TextView stgst = activity.findViewById(R.id.stglistst);

        stglist.setVisibility(View.VISIBLE);
        stgprog.setVisibility(View.GONE);
        stgst.setVisibility(View.GONE);
    }
}