package com.afollestad.smshelper;

import java.util.Hashtable;

public class ContactCache {

	public ContactCache() {
		nameCache = new Hashtable<Long, String>();
		addressCache = new Hashtable<Long, String>();
	}
	
	private Hashtable<Long, String> nameCache;
	private Hashtable<Long, String> addressCache;
	
	public Contact getFromNumber(String number) {
		if(!addressCache.containsValue(number)) {
			return null;
		}
		String[] values = addressCache.values().toArray(new String[0]);
		for(int i = 0; i < values.length; i++) {
			if(values[i].equals(number)) {
				System.out.println("Got contact from cache: " + number);
				return getFromId(addressCache.keySet().toArray(new Long[0])[i]);
			}
		}
		return null;
	}
	
	public Contact getFromId(Long id) {
		if(!containsId(id)) {
			return null;
		}
		System.out.println("Got contact from cache: " + id + " (" + addressCache.get(id).toString() + ")");
		return new Contact(id, nameCache.get(id), addressCache.get(id));
	}
	
	public void put(Long id, Contact contact) {
		nameCache.put(id, contact.getName());
		addressCache.put(id, contact.getNumber());
	}
	
	public boolean containsId(Long id) {
		return (nameCache.containsKey(id) && addressCache.containsKey(id));
	}
	
	public boolean containsNumber(String number) {
		return addressCache.containsValue(number);
	}
}