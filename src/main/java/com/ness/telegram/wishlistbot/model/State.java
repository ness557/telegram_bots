package com.ness.telegram.wishlistbot.model;

/**
 * State
 * states of user dialog
 * Default - user doing nothing
 */
public enum State {
    DEFAULT,
    ADD_SETLABEL,
    ADD_SETLINK,
    ADD_SETPRICE,
    DELETE_CHOOSE,
    DELETE_CONFIRM,
    EDIT_CHOOSE,
    EDIT_SETLABEL,
    EDIT_SETLINK,
    EDIT_SETPRICE;   
}