package com.boxtrotstudio.ghost.game.match.item;

import com.boxtrotstudio.ghost.game.match.entities.map.Text;

import java.util.Arrays;
import java.util.Iterator;

public class Inventory implements Iterable<Item> {
    private Item[] items;

    public Inventory(int size) {
        items = new Item[size];
    }

    public int getSize() {
        return items.length;
    }

    public void setSize(int size) {
        Item[] _items = new Item[size];
        for (int i = 0; i < items.length; i++) {
            if (i >= _items.length)
                break;

            _items[i] = items[i];
        }

        this.items = _items;
    }

    public Item getItem(int slot) {
        return items[slot];
    }

    public boolean hasItem(int slot) {
        return items[slot] != null;
    }

    public int itemCount() {
        int c = 0;
        for (Item item : items) {
            if (item != null)
                c++;
        }

        return c;
    }

    public int addItem(Item item) {
        int slot = firstOpenSlot();
        if (slot == -1)
            throw new IllegalStateException("No more open slots!");

        items[slot] = item;

        return slot;
    }

    public void set(Item item, int slot) {
        if (slot >= items.length)
            throw new IllegalArgumentException("slot must be less then getSize()!");

        items[slot] = item;
    }

    public void remove(int slot) {
        set(null, slot);
    }

    public boolean isFull() {
        return firstOpenSlot() == -1;
    }

    public int firstOpenSlot() {
        int index = -1;
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                index = i;
                break;
            }
        }

        return index;
    }

    @Override
    public Iterator<Item> iterator() {
        return Arrays.asList(items).iterator();
    }

    public void clear() {
        for (int i = 0; i < items.length; i++) {
            items[i] = null;
        }
    }
}
