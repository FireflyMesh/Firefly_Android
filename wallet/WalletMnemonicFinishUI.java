package com.lingtuan.firefly.wallet;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.flowtag.FlowTagLayout;
import com.lingtuan.firefly.custom.flowtag.OnTagSelectListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.MnemonicSelectVo;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created on 2017/8/22.
 * According to the private key plaintext
 * {@link WalletCopyActivity}
 */

public class WalletMnemonicFinishUI extends BaseActivity{

    @BindView(R.id.walletCopyPrivateKey)
    TextView walletCopyPrivateKey;
    @BindView(R.id.mnemonicAddFlowTag)
    FlowTagLayout mnemonicAddFlowTag;
    @BindView(R.id.mnemonicRemoveFlowTag)
    FlowTagLayout mnemonicRemoveFlowTag;

    private MnemonicAdapter  mMnemonicAdapter;
    private MnemonicRemoveAdapter  mMnemonicRemoveAdapter;

    private ArrayList<MnemonicSelectVo> mnemonicList;

    private ArrayList<MnemonicSelectVo> tempMnemonicList;

    private String mnemonic;
    private String walletAddress;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_show_mnemonic_layout);
        getPassData();
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    private void getPassData() {
        mnemonic = getIntent().getStringExtra(Constants.MNEMONIC);
        walletAddress = getIntent().getStringExtra(Constants.WALLET_ADDRESS);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_show_mnemonic_2));
        if (!TextUtils.isEmpty(mnemonic)){
            mnemonicList = new ArrayList<>();
            tempMnemonicList = new ArrayList<>();
            String[] mnemonics = mnemonic.split(" ");
            MnemonicSelectVo mnemonicSelectVo;
            for (int i = 0 ; i < mnemonics.length ; i++){
                mnemonicSelectVo = new MnemonicSelectVo();
                mnemonicSelectVo.setHasSelected(false);
                mnemonicSelectVo.setMnemonic(mnemonics[i]);
                mnemonicList.add(mnemonicSelectVo);
            }
        }

        mMnemonicAdapter = new MnemonicAdapter(this);
        mnemonicAddFlowTag.setAdapter(mMnemonicAdapter,false);

        mMnemonicRemoveAdapter = new MnemonicRemoveAdapter(this);
        mnemonicRemoveFlowTag.setAdapter(mMnemonicRemoveAdapter,true);

        mMnemonicAdapter.loadData(mnemonicList);
        mMnemonicRemoveAdapter.loadData(tempMnemonicList);

        mnemonicAddFlowTag.setOnTagSelectListener(new OnTagSelectListener() {
            @Override
            public void onItemSelect(FlowTagLayout parent, MnemonicSelectVo tempMnemonic) {
                tempMnemonicList.add(tempMnemonic);
                for (int j = 0 ; j < mnemonicList.size() ; j++){
                    if (mnemonicList.get(j).getMnemonic().equals(tempMnemonic.getMnemonic())){
                        mnemonicList.get(j).setHasSelected(true);
                    }
                }
                mMnemonicAdapter.reloadAll(mnemonicList);
                mMnemonicRemoveAdapter.reloadAll(tempMnemonicList);
            }
        });

        mnemonicRemoveFlowTag.setOnTagSelectListener(new OnTagSelectListener() {
            @Override
            public void onItemSelect(FlowTagLayout parent, MnemonicSelectVo mnemonicSelectVo) {
                if (mnemonicSelectVo == null){
                    return;
                }
                tempMnemonicList.remove(mnemonicSelectVo);
                for (int j = 0 ; j < mnemonicList.size() ; j++){
                    if (mnemonicList.get(j).getMnemonic().equals(mnemonicSelectVo.getMnemonic())){
                        mnemonicList.get(j).setHasSelected(false);
                    }
                }
                mMnemonicAdapter.reloadAll(mnemonicList);
                mMnemonicRemoveAdapter.reloadAll(tempMnemonicList);
            }
        });
    }

    /**
     * covert mnemonic string to List<String>
     */
    private static String convertMnemonicList(List<MnemonicSelectVo> mnemonics) {
        StringBuilder sb = new StringBuilder();
        for (MnemonicSelectVo mnemonic : mnemonics) {
            sb.append(mnemonic.getMnemonic());
            sb.append(" ");
        }
        return sb.toString();
    }

    @OnClick(R.id.walletCopyPrivateKey)
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.walletCopyPrivateKey:
                String tempMnemonic =convertMnemonicList(tempMnemonicList);
                if (TextUtils.equals(tempMnemonic.trim(),mnemonic.trim())){
                    deleteMnemonic();
                }else{
                    backupError();
                }
                break;
            default:
                super.onClick(v);
        }
    }

    private void deleteMnemonic(){
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.wallet_export_prompt),getString(R.string.wallet_mnemonic_6));
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                copyMnemonic();
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }


    private void backupError(){
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_SINGLE_CENTER_BUTTON);
        mdf.setTitleAndContentText(getString(R.string.wallet_mnemonic_4),getString(R.string.wallet_mnemonic_11));
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    private void copyMnemonic(){
        updateCopyState();
        setResult(RESULT_OK);
        Utils.exitActivityAndBackAnim(WalletMnemonicFinishUI.this,true);
    }

    public void updateCopyState(){
        ArrayList<StorableWallet> walletList = WalletStorage.getInstance(getApplicationContext()).get();
        for (int i = 0; i < walletList.size(); i++) {
            if (walletList.get(i).getPublicKey().equals(walletAddress)) {
                walletList.get(i).setBackup(true);
                break;
            }
        }
        WalletStorage.getInstance(getApplicationContext()).updateWalletToList(getApplicationContext(), walletAddress, true);
        Utils.sendBroadcastReceiver(getApplicationContext(), new Intent(Constants.WALLET_REFRESH_BACKUP), false);
    }
}
