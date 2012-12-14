package com.afollestad.smshelper;

import java.util.Hashtable;

import android.content.Context;

public class ContactCache {

	public ContactCache(Context context) {
		cache = new Hashtable<Long, Contact>();
	}
	
	private Hashtable<Long, Contact> cache;
	
	public Contact getFromNumber(String number) {
		int contains = containsNumber(number);
		if(contains == -1) {
			return null;
		}
		Contact toreturn = cache.values().toArray(new Contact[0])[contains];
		System.out.println("Got contact from cache: " + toreturn.getId() + " (" + toreturn.getName() + ")");
		return toreturn;
	}
	
	public Contact getFromEmail(String address) {
		int contains = containsEmail(address);
		if(contains == -1) {
			return null;
		}
		Contact toreturn = cache.values().toArray(new Contact[0])[contains];
		System.out.println("Got contact from cache: " + toreturn.getId() + " (" + toreturn.getName() + ")");
		return toreturn;
	}
	
	public Contact getFromId(Long id) {
		if(!containsId(id)) {
			return null;
		}
		Contact toreturn = cache.get(id);
		System.out.println("Got contact from cache: " + toreturn.getId() + " (" + toreturn.getName() + ")");
		return toreturn;
	}
	
	public void put(Long id, Contact contact) {
		if(contact == null) {
			return;
		}
		cache.put(id, contact);
	}
	
	public boolean containsId(Long id) {
		return cache.containsKey(id);
	}
	
	public int containsNumber(String number) {
		int index = -1;
		for(Long key : cache.keySet()) {
			index++;
			Contact value = cache.get(key);
			if(!value.isEmail() && value.getAddress().equals(number)) {
				return index;
			}
		}
		return -1;
	}

	public int containsEmail(String address) {
		int index = -1;
		for(Long key : cache.keySet()) {
			index++;
			Contact value = cache.get(key);
			if(value.isEmail() && value.getAddress().equals(address)) {
				return index;
			}
		}
		return -1;
	}
}