package com.example.Secretgram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class TabsAccessorAdapter extends FragmentStatePagerAdapter {


    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position)
        {
            case 0:
                return new ChatsFragment();

            case 1:
                return new GroupFragment();

            case 2:
                return new ContactsFragment();

            case 3:
                return new RequestsFragment();

            default:
                return null;

        }
    }

    @Override
    public int getCount() {

        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Chats";

            case 1:
                return "Group";


            case 2:
                return "Cotacts";

            case 3:
                return "Requests";

            default:
                return null;

        }
    }
}
