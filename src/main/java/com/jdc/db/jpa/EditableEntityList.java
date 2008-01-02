/*
 * EditableRecordList.java
 *
 * Created on March 9, 2006
 *
 * To keep a clear separation of business logic and UI code:
 * In order to Edit or View and item from this list you MUST do the following:
 *   - Create new classes that implement RecordItemEditor AND RecordItemViewer
 *   - Assign instances of these classes to this list by calling setItemEditor(...) AND setItemViewer(...)
 *
 * Copyright 2006 Jeff Campbell. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package com.jdc.db.jpa;

import com.jdc.db.jpa.query.JPAQueryBuilder;
import com.jdc.components.DefaultListItem;
import com.jdc.components.JLookupComboBox;
import com.jdc.db.jpa.components.EntityTableRowData;
import com.jdc.db.jpa.components.EntityItemEditor;
import com.jdc.db.jpa.components.EntityItemViewer;
import com.jdc.db.jpa.components.JDBEntityColumn;
import com.jdc.db.jpa.components.JDBEntityColumnFilter;
import com.jdc.db.jpa.components.JPAEntityTable;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.swing.JOptionPane;

/**
 *
 * @author Jeff
 */
public abstract class EditableEntityList<T extends Object> {

    private Class queryBuilderClass = JPAQueryBuilder.class;
    private JPAQueryBuilder baseQuery; // this is the base query and should as little filters as possible

    private int baseQueryIDColumn = -1;
    private List<DefaultListItem> itemList;
    private Map<Integer, List<DefaultListItem>> filteredItemLists = new HashMap<Integer, List<DefaultListItem>>();
    //private Map<Integer, List<DefaultListItem>> filteredLists = new HashMap<Integer, List<DefaultListItem>>();
    private List<TableGroup> tableGroups = new ArrayList<TableGroup>();
    private Map<JPAEntityTable, TableGroup> tableGroupsMap = new HashMap<JPAEntityTable, TableGroup>();
    private List<JLookupComboBoxGroup> comboBoxGroups = new ArrayList<JLookupComboBoxGroup>();
    private int editClickCount = 2;
    private EntityItemEditor<T> itemEditor;
    private EntityItemViewer<T> itemViewer;
    private String objectName; 
    private String idColumnName;

    /** Creates a new instance of EditableRecordList */
    public EditableEntityList(String objectName, String idColumnName) {
        this(JPAQueryBuilder.class, objectName, idColumnName);
    }
    
    /** Creates a new instance of EditableRecordList */
    public EditableEntityList(Class queryBuilderClass, String objectName, String idColumnName) {
        if (objectName == null || objectName.isEmpty()) {
            throw new IllegalArgumentException("objectName cannot be null or empty");
        }
        if (idColumnName == null || idColumnName.isEmpty()) {
            throw new IllegalArgumentException("idColumnName cannot be null or empty");
        }

        this.queryBuilderClass = queryBuilderClass;
        this.objectName = objectName;
        this.idColumnName = idColumnName;

        // do some initialization
        createBaseQuery();

    // add listeners to RecordManager
//        EntityManager manager = getRecordManager(entityManager);
//        manager.addRecordChangeListener(this);
    }

    private JPAQueryBuilder createQueryBuilder() {
        try {
            return (JPAQueryBuilder) queryBuilderClass.newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(EditableEntityList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EditableEntityList.class.getName()).log(Level.SEVERE, null, ex);
        }

        throw new IllegalStateException("Could not create query builder");
    }

    private void createBaseQuery() {
        baseQuery = createQueryBuilder();

        baseQuery.addObject(objectName);
        baseQueryIDColumn = baseQuery.addField(idColumnName);
        // base field columns
        for (JDBEntityColumn column : getJDBEntityColumnsForTable()) {
            baseQuery.addField(column.getVarClassName());

            if (column.hasJoin()) {
                column.addJoinToQuery(baseQuery);
            }
        }

        // sort columns
        String[] orderByColumns = getOrderByColumns();
        if (orderByColumns != null) {
            for (int i = 0; i < orderByColumns.length; i++) {
                baseQuery.addOrderBy(orderByColumns[i]);
            }
        }
    }

    public abstract EntityManager getEntityManager();

    public abstract String[] getOrderByColumns();

    /**
     * Used by message dialogs to reference this item
     *
     * Example: When this item is about to be deleted, a pop-up dialog will
     * prompt the user: "Do you wish to delete "+ getItemName() +"?"
     */
    public abstract String getItemName(T item);

    /**
     * Given the int id key, return the matching entity for this id.  If none is
     * found, then return null
     * @return found item, or null if not found
     */
    public abstract T findItemByID(int id);

    /**
     * Given the Object, return the id for the given Entity.
     * @return id for item
     */
    public abstract int getItemID(T item);

    /**
     * Given the Object, is the item new and unsaved? (example ID == 0)
     * @return true if item is new and unsaved
     */
    public abstract boolean isItemNewAndUnsaved(T item);

    /**
     * Return new instance of an entity for this list
     * @return newly created item
     */
    public abstract T createNewEntity();
    /**
     * Save item
     * @return Item that was saved or null if item did not save
     */
    //public abstract T saveItem(T item);

    /**
     * Columns that contains the text that will be shown in Comboboxes
     */
    public abstract String[] getComboBoxTextDBColumnNames();

    /**
     * If there are more than 1 column in the getComboBoxTextDBColumnNames()...
     * use this text to separate columns.
     *
     * For most cases returning " " could be sufficient.
     * If null is returned here then " " is used
     */
    public abstract String getComboBoxTextDBColumnNamesSeparator();

    /**
     * Get list of columns to be used by table and search
     */
    public abstract List<JDBEntityColumn> getJDBEntityColumnsForTable();
    
    /**
     * JTextField used by table to filter content.  If null is returned, then
     * no search field will be used.
     *
     * @return JTextField of text field that will be used in filtering search.  Set to null if none.
     */
    //    public abstract JTextField getQuickSearchTextField();

    /**
     * Check to see if this item really can be deleted.  (check for dependencies, etc)
     * If this method ever returns false then getDeleteFailedErrorMessage() will be called
     * to retrieve the reason why it could not be deleted
     * @param item Item to be checked for delete
     * @return true if item CAN be deleted
     */
    public abstract boolean canItemBeDeleted(T item);

    /**
     * I an item cannot be deleted (because canItemBeDeleted(...) returned false),
     * What text should be shown to user?
     *
     * @return delete attempt error message
     */
    public abstract String getDeleteFailedErrorMessage();

    /**
     * Set any default values prior to showing the item to the user
     */
    public abstract void setNewItemDefaults(T item);

    /**
     * When a JLookupComboBox trys to "Quick-add" a new item, what should
     * the defaults be?
     * @param item New Item that was created by the table (before it is shown to the user)
     * @param textFromComboBox Current text that was pulled from the JLookupComboBox
     */
    public abstract void setComboBoxNewItemDefaults(T item, String textFromComboBox);

    /**
     * First item that should show in any attached JLookupComboBox's.  If null is
     * returned, then there is NO default item.
     */
    public abstract DefaultListItem getDefaultComboItem();
    // ***************** END of Abstract methods *********************

    private TableGroup getTableGroup(JPAEntityTable dbTable) {
        return tableGroupsMap.get(dbTable);
    }

    private Component getParentComponent(JPAEntityTable dbTable) {
        if (dbTable == null) {
            throw new NullPointerException("table cannot be null");
        }

        TableGroup group = getTableGroup(dbTable);
        if (group == null) {
            throw new IllegalArgumentException("Cannot find TableGroup for given table");
        }

        return group.getParent();
    }

    /**
     * Resets Lists because something changed to List (Item added, deleted, modified, etc)
     */
    public void refreshList() {
        try {
            invalidateLists();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Resets Lists because something changed to List (Item added, deleted, modified, etc)
     */
    private void invalidateLists() throws SQLException {
        // update tables
        for (TableGroup tableGroup : tableGroups) {
            JPAEntityTable table = tableGroup.getTable();

            // get selected id.... so that the last id may be restored
            int lastID = table.getSelectedID();

            List<EntityTableRowData> tableData = getTableData(tableGroup);
            table.setTableData(tableData);

            // restore selection (if possible)
            if (lastID > 0) {
                table.setSelectedID(lastID);
            }
        }

        // update combo boxes
        itemList = null; // this will force a new list to be create (only one will be created for ALL combo boxes)
        filteredItemLists.clear();
        for (JLookupComboBoxGroup comboBoxGroup : comboBoxGroups) {
            JLookupComboBox comboBox = comboBoxGroup.getComboBox();
            int currentSelectedID = comboBox.getSelectedID();

            if (!comboBoxGroup.isFiltered()) {
                comboBox.setData(getList());
            } else {
                EditableEntityListFilter filter = comboBoxGroup.getFilter();
                comboBox.setData(getList(false, filter));
            }

            // restore selection... if possible
            if (currentSelectedID > 0) {
                comboBox.setSelectedID(currentSelectedID);
            }
        }
    }

    private class TableMouseAdapter extends java.awt.event.MouseAdapter {

        private JPAEntityTable table;

        public TableMouseAdapter(JPAEntityTable table) {
            this.table = table;
        }

        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getClickCount() == editClickCount) {
                showItem(table, table.getSelectedID(), true);
            }
        }
    }

    private class TableKeyAdapter extends java.awt.event.KeyAdapter {

        private JPAEntityTable dbTable;

        public TableKeyAdapter(JPAEntityTable dbTable) {
            this.dbTable = dbTable;
        }

        @Override
        public void keyReleased(KeyEvent evt) {
            switch (evt.getKeyCode()) {
                case KeyEvent.VK_DELETE:
                    try {
                        deleteSelectedItem(dbTable);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    break;
                default:
                // ignore... move on...
            }
        }
    }

    private void checkForNullTable(JPAEntityTable dbTable) {
        if (dbTable == null) {
            throw new IllegalArgumentException("dbTable cannot be null");
        }
    }

    public void executeSearch(JPAEntityTable dbTable) {
        TableGroup tableGroup = getTableGroup(dbTable);
        if (tableGroup == null) {
            throw new IllegalArgumentException("Cannot find TableGroup for given table");
        }

        List<EntityTableRowData> tableData = getTableData(tableGroup);
        dbTable.setTableData(tableData);
    }

    public T addItem(JPAEntityTable dbTable) {
        return showItem(dbTable, 0, true); // 0 means "make new record"
    }

    public T editSelectedItem(JPAEntityTable dbTable) {
        checkForNullTable(dbTable);
        return showItem(dbTable, dbTable.getSelectedID(), true);
    }

    public T viewSelectedItem(JPAEntityTable dbTable) {
        checkForNullTable(dbTable);
        return showItem(dbTable, dbTable.getSelectedID(), false);
    }

    public boolean deleteSelectedItem(JPAEntityTable dbTable) throws SQLException {
        checkForNullTable(dbTable);
        boolean deleted = false;
        int itemID = dbTable.getSelectedID();

        if (itemID == -1) {
            JOptionPane.showMessageDialog(getParentComponent(dbTable), "Error", "Cannot delete item, no item selected.", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // find the item
        T item = findItemByID(itemID);
        if (item == null) {
            JOptionPane.showMessageDialog(getParentComponent(dbTable), "Error", "Cannot find item to delete, id=[" + itemID + "]", JOptionPane.ERROR_MESSAGE);
        }

        int result = JOptionPane.showConfirmDialog(getParentComponent(dbTable), "Are you sure you want to delete [" + getItemName(item) + "]?", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            if (deleteItem(item)) {
                deleted = true;
            } else {
                JOptionPane.showMessageDialog(getParentComponent(dbTable), "Cannot delete item: " + getDeleteFailedErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        return deleted;
    }

    public boolean deleteItem(int itemID) throws SQLException {
        T item = findItemByID(itemID);

        if (item == null) {
            throw new IllegalArgumentException("could not deleted itemID [" + itemID + "]... Item NOT found.");
        }

        return deleteItem(item);
    }

    public boolean deleteItem(T item) throws SQLException {
        boolean deleted = false;
        if (canItemBeDeleted(item)) {
            EntityManager em = getEntityManager();
            em.getTransaction().begin();
            T mergedItem = em.merge(item);
            em.remove(mergedItem);
            em.getTransaction().commit();
            em.close();

            // update list
            refreshList();
            deleted = true;
        }

        return deleted;
    }

    public T saveItem(T item) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        T savedItem = null;
        if (isItemNewAndUnsaved(item)) {
            em.persist(item);
        } else {
            savedItem = em.merge(item);
        }

        em.getTransaction().commit();
        em.close();

        refreshList(); // Handled by Entity Listener
        return savedItem;
    }

    private T showItem(JPAEntityTable dbTable, int itemID, boolean editMode) {
        T item = null;

        try {
            if (itemID == 0) {
                // create a new record
                item = createNewEntity();

                // set some defaults.... if any
                setNewItemDefaults(item);
            } else if (itemID < 0) {
                String t = "show";
                if (editMode) {
                    t = "edit";
                }

                JOptionPane.showMessageDialog(getParentComponent(dbTable), "Error", "Cannot " + t + " item, no item selected.", JOptionPane.ERROR_MESSAGE);
            } else {
                // load existing
                item = findItemByID(itemID);

                if (item == null) {
                    JOptionPane.showMessageDialog(getParentComponent(dbTable), "Error", "Show error. Cannot find item ID [" + itemID + "]", JOptionPane.ERROR_MESSAGE);
                }
            }

            item = showItem(getParentComponent(dbTable), item, editMode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    private T showItem(Component parent, T item, boolean editMode) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }

        // make sure parent is either a Dialog or Frame
        if (parent instanceof Dialog || parent instanceof Frame) {
        // OK!
        } else {
            throw new IllegalStateException("Cannot show item if parent component is NOT a Dialog or Frame");
        }

        try {
            // create and show food item dialog
            if (editMode) {
                editItem(parent, item);
            } else {
                viewItem(parent, item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    public boolean editItem(Component parent, T item) {
        if (itemEditor == null) {
            throw new IllegalStateException("EntityItemEditor has NOT been set for this list (Table: [" + objectName + "]).  Be sure to call setItemEditor(...) before using this list to edit items.");
        }

        return itemEditor.editItem(parent, item);
    }

    public void viewItem(Component parent, T item) {
        if (itemViewer == null) {
            throw new IllegalStateException("EntityItemViewer has NOT been set for this list (Table: [" + objectName + "]).  Be sure to call setItemViewer(...) before using this list to view items.");
        }
        itemViewer.viewItem(parent, item);
    }

    /**
     * Attaches a Table to the primary list and can allow a quick way of adding new items
     *
     * NOTE: REMEMBER TO CALL detachLookupComboBox(...) or you will have a memory leak!!!!!
     */
    public void attachTable(JPAEntityTable dbTable, Component parent, List<JDBEntityColumnFilter> componentFilters) {
        attachTable(dbTable, parent, componentFilters, null);
    }

    /**
     * Attaches a Table to the primary list and can allow a quick way of adding new items
     *
     * NOTE: REMEMBER TO CALL detachLookupComboBox(...) or you will have a memory leak!!!!!
     */
    public void attachTable(JPAEntityTable dbTable, Component parent, List<JDBEntityColumnFilter> componentFilters, EditableEntityListFilter filter) {
        if (dbTable == null) {
            throw new IllegalArgumentException("table cannot be null");
        }

        TableGroup tableGroup = new TableGroup(dbTable, parent, componentFilters, filter);

        // setup table
        dbTable.setEditableEntityList(this);
        initTable(dbTable);

        // store for later
        tableGroups.add(tableGroup);
        tableGroupsMap.put(dbTable, tableGroup);

        // execute initial search
        executeSearch(dbTable);

        // add listener so that the table can auto-detatch
        if (parent instanceof Window) {
            ((Window) parent).addWindowListener(new java.awt.event.WindowAdapter() {

                        @Override
                public void windowClosing(java.awt.event.WindowEvent evt) {
                            Iterator<TableGroup> itr = tableGroups.iterator();
                            while (itr.hasNext()) {
                                TableGroup tableGroup = itr.next();
                                if (tableGroup.getParent() == evt.getWindow()) {
                                    tableGroupsMap.remove(tableGroup.getTable());
                                    itr.remove();
                                }
                            }
                        }
                    });
        }
    }

    public void detachTable(JPAEntityTable table) {
        // find the table
        boolean found = false;
        for (int i = 0; !found && i < tableGroups.size(); i++) {
            TableGroup tableGroup = tableGroups.get(i);

            if (tableGroup.getTable() == table) {
                tableGroups.remove(i);
                found = true;
            }
        }

    // if not found... throw exception
//        if (!found) {
//            throw new IllegalStateException("Could NOT detach Table.  Table NOT found to detach");
//        }
    }

    private void initTable(JPAEntityTable table) {
        // create table adapters
        table.addMouseListener(new TableMouseAdapter(table));
        table.addKeyListener(new TableKeyAdapter(table));

    // initialize table
        // table columns will be added on the first query
//        initRecordColumns(table);
    }
    //    private void initRecordColumns(JPAEntityTable table) {
//
//        List<JDBRecordColumn> columns = getRecordColumns();
//
//        int col = 2; // start at 2 becuase the first column is to
//        for(JDBRecordColumn column : columns) {
//            table.addColumn(column.getDbFieldName(), column.getColumnName(), column.getColumnWidth(), column.isVisible());
//            col++;
//        }
//
//    }

    /**
     * Attaches a JLookupComboBox to the primary list and can allow a quick way of adding new items
     *
     * NOTE: REMEMBER TO CALL detachLookupComboBox(...) or you will have a memory leak!!!!!
     */
    public void attachLookupComboBox(JLookupComboBox itemLookupComboBox, Component parent, boolean allowQuickAdd) {
        attachLookupComboBox(itemLookupComboBox, parent, allowQuickAdd, null);
    }

    /**
     * Attaches a JLookupComboBox to the primary list and can allow a quick way of adding new items
     *
     * NOTE: REMEMBER TO CALL detachLookupComboBox(...) or you will have a memory leak!!!!!
     */
    public void attachLookupComboBox(JLookupComboBox itemLookupComboBox, Component parent, boolean allowQuickAdd, EditableEntityListFilter filter) {
        if (itemLookupComboBox == null) {
            throw new IllegalArgumentException("itemLookupComboBox cannot be null");
        }

        JLookupComboBoxGroup comboGroup = new JLookupComboBoxGroup(itemLookupComboBox, parent, filter);

        // add quick-add functionality (if applicable)
        if (allowQuickAdd) {
            // set some default attributes
            itemLookupComboBox.setEnforceItemFromCombo(false);

            // add listeners
            itemLookupComboBox.addFocusListener(new ComboBoxFocusAdapter(comboGroup));
            itemLookupComboBox.addKeyListener(new ComboBoxKeyAdapter(comboGroup));
        }

        // fill the list
        try {
            itemLookupComboBox.setData(getList());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // store for later (auto refills)
        comboBoxGroups.add(comboGroup);

        // add listener so that the combobox can auto-detatch
        if (parent instanceof Window) {
            ((Window) parent).addWindowListener(new java.awt.event.WindowAdapter() {

                        @Override
                public void windowClosing(java.awt.event.WindowEvent evt) {
                            Iterator<JLookupComboBoxGroup> itr = comboBoxGroups.iterator();
                            while (itr.hasNext()) {
                                JLookupComboBoxGroup comboGroup = itr.next();
                                if (comboGroup.getParent() == evt.getWindow()) {
                                    itr.remove();
                                }
                            }
                        }
                    });
        }
    }

    public void detachLookupComboBox(JLookupComboBox itemLookupComboBox) {
        // find the itemLookupComboBox
        boolean found = false;
        for (int i = 0; !found && i < comboBoxGroups.size(); i++) {
            JLookupComboBoxGroup comboGroup = comboBoxGroups.get(i);

            if (comboGroup.getComboBox() == itemLookupComboBox) {
                comboBoxGroups.remove(i);
                found = true;
            }
        }

        // if not found... throw exception
        if (!found) {
            throw new IllegalStateException("Could NOT detach JLookupComboBox.  JLookupComboBox NOT found to detach");
        }
    }

    private class ComboBoxFocusAdapter extends java.awt.event.FocusAdapter {

        private JLookupComboBoxGroup comboBoxGroup;

        public ComboBoxFocusAdapter(JLookupComboBoxGroup comboBoxGroup) {
            this.comboBoxGroup = comboBoxGroup;
        }

        @Override
        public void focusLost(java.awt.event.FocusEvent evt) {
            if (!alreadyCheckingForNewItem) {
                checkForNewItem(comboBoxGroup);
            }
        }
    }

    private class ComboBoxKeyAdapter extends java.awt.event.KeyAdapter {

        private JLookupComboBoxGroup comboBoxGroup;

        public ComboBoxKeyAdapter(JLookupComboBoxGroup comboBoxGroup) {
            this.comboBoxGroup = comboBoxGroup;
        }

        @Override
        public void keyReleased(KeyEvent evt) {
            switch (evt.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    if (!alreadyCheckingForNewItem) {
                        checkForNewItem(comboBoxGroup);
                    }

                    break;
                default:
                // ignore... move on...
            }
        }
    }
    private boolean alreadyCheckingForNewItem = false;

    private void checkForNewItem(JLookupComboBoxGroup comboGroup) {
        alreadyCheckingForNewItem = true;
        try {
            JLookupComboBox itemLookupComboBox = comboGroup.getComboBox();
            int selectedID = itemLookupComboBox.getSelectedID();

            if (selectedID == -1 && !itemLookupComboBox.getText().equals("")) {
                String comboText = itemLookupComboBox.getText();
                int ret = JOptionPane.showConfirmDialog(comboGroup.getParent(), "Item [" + comboText + "] does not exist, would you like to add it now?", "New Item", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (ret == JOptionPane.YES_OPTION) {
                    T newItem = createNewEntity();

                    setComboBoxNewItemDefaults(newItem, comboText);

                    showItem(comboGroup.getParent(), newItem, true);

                    // NO NEED to invalidateLists() here.... that is done by RecordChangeListener
                    // Item should be saved by now.... which should now give it a new table ID.
                    // so.... let's select the new id in the combobox
                    int newID = getItemID(newItem);


                    if (newID <= 0) {
                        System.out.println("WARNING: Either the user cancelled adding new item OR developer code did NOT commit data (item.commit() then dbManager.commit())");
                    // DO NOT THROW EXCEPTION!!!!  USER COULD HAVE CANCELLED THE ADD
                        //    throw new IllegalStateException("New item that was created did NOT get saved because item.getID() was invalid [].  Make sure to do the following before closing edit item dialog: \n1. item.commit()\n2. dbManager.commit()");
                    }

                    itemLookupComboBox.setSelectedID(newID);
                } else {
                    // if no.... go to default item
                    if (getDefaultComboItem() != null) {
                        itemLookupComboBox.setSelectedID(getDefaultComboItem().getListID());
                    } else {
                        itemLookupComboBox.setSelectedID(-1);
                    }
                }
            }
        } finally {
            alreadyCheckingForNewItem = false;
        }
    }

    public List<EntityTableRowData> getTableData(TableGroup tableGroup) {
        EntityManager em = getEntityManager();

        if (em == null) {
            throw new IllegalStateException("getEntityManager() is returning null!  Be sure to initialize entityManager prior to or when getEntityManager() is called.");
        }

        // make a copy of the base query so that it is not modified
        JPAQueryBuilder qb = (JPAQueryBuilder) baseQuery.clone();

        qb.setEntityManager(em);

        // add columns to query
        JPAEntityTable table = tableGroup.getTable();

        // add ui columns to table if they have not already been added
        if (!tableGroup.isColumnsAlreadyAdded()) {
            int i = 1; // start at 1 because ID column has already been added
            for (JDBEntityColumn column : getJDBEntityColumnsForTable()) {
                //table.addColumn(i, column.getColumnName(), column.getColumnWidth(), column.isVisible());
                table.setSqlColToUiColMap(i, column.getJTable2ColID());
                table.setJDBEntityColumnBySQLColID(i, column);
                i++;
            }

            tableGroup.setColumnsAlreadyAdded(true);
        // refresh table UI
        }

        // add any filters
        EditableEntityListFilter filter = tableGroup.getFilter();
        if (filter != null) {
            filter.appendFilter(qb);
        }

        // add additional component filters as needed
        for (JDBEntityColumnFilter compFilter : tableGroup.getComponentFilters()) {
            switch (compFilter.getFilterType()) {
                case INT:
                    if (!compFilter.ignoreComponentValue()) {
                        int intValue = (Integer) compFilter.getValue();
                        qb.addFilter(compFilter.getClassVarName(), compFilter.getCompareType(), intValue, compFilter.getOrGroupKey());
                    }
                    break;
                case STRING:
                    if (!compFilter.ignoreComponentValue()) {
                        String value = (String) compFilter.getValue();
                        qb.addFilter(compFilter.getClassVarName(), compFilter.getCompareType(), value, compFilter.getOrGroupKey());
                    }
                    break;
                case DATE:
                    if (!compFilter.ignoreComponentValue()) {
                        Date value = (Date) compFilter.getValue();
                        qb.addFilter(compFilter.getClassVarName(), compFilter.getCompareType(), value, compFilter.getOrGroupKey());
                    }
                    break;
                default:
                    throw new IllegalStateException("filter for column [" + compFilter.getClassVarName() + "] could not be added because it had an unknown value type");
            }
        }

        // add order by columns  (no need.... it is already in the base Query
        // 1. Execute query
        // 2. Put results in TableRowData object
        List<EntityTableRowData> tableDataResults = new ArrayList<EntityTableRowData>();
        Query query = qb.executeQuery();
        List<Object[]> rs = (List<Object[]>) query.getResultList();
        for (Object[] resultRow : rs) {
            EntityTableRowData dbTableRowData = new EntityTableRowData(baseQueryIDColumn);

            for (int col = 0; col < resultRow.length; col++) {
                //row[col] = r.getString(columns[col]);
                if (col == baseQueryIDColumn) {
                    dbTableRowData.setTableDataID((Integer) resultRow[col]);
                } else {
                    int colID = table.getUiColID(col);
                    JDBEntityColumn entityCol = table.getJDBEntityColumnBySQLColID(col);

                    Class colClassType = entityCol.getClassType();
                    if (colClassType == boolean.class || colClassType == Boolean.class) {
                        Object data = resultRow[col];
                        if (data instanceof Number) {
                            Number n = (Number) data;
                            if (n.intValue() == 0) {
                                dbTableRowData.setTableDataValueAt(colID, Boolean.FALSE);
                            } else {
                                dbTableRowData.setTableDataValueAt(colID, Boolean.TRUE);
                            }
                        }
                    } else {
                        dbTableRowData.setTableDataValueAt(colID, resultRow[col]);
                    }
                }
            }

            tableDataResults.add(dbTableRowData);
        }

        em.close();
        qb.close();

        return tableDataResults;
    }

    public List<DefaultListItem> getList() throws SQLException {
        return getList(false);
    }

    public List<DefaultListItem> getList(boolean refresh) throws SQLException {
        return getList(refresh, null);
    }

    private List<DefaultListItem> getList(boolean refresh, EditableEntityListFilter filter) throws SQLException {
        int listID = -1;
        if (filter != null) {
            listID = filter.getFilterID();
        }

        List<DefaultListItem> workingList;

        boolean initList = false;
        if (listID < 0) {
            workingList = itemList;

            if (workingList == null) {
                initList = true;
                workingList = new ArrayList<DefaultListItem>();
            }
        } else {
            workingList = filteredItemLists.get(listID);

            if (workingList == null) {
                initList = true;
                workingList = new ArrayList<DefaultListItem>();
                filteredItemLists.put(listID, workingList);
            }
        }

        // create list
        if (initList) {
            refresh = true; // force
            if (getDefaultComboItem() != null) {
                workingList.add(getDefaultComboItem());
            }
        }

        // populate list
        if (refresh) {
            EntityManager em = getEntityManager();
            JPAQueryBuilder qb = createQueryBuilder();
            qb.setEntityManager(em);
            qb.addObject(objectName);
            qb.addField(idColumnName);

            // add combo columns
            String[] columns = getComboBoxTextDBColumnNames();
            int numColumns = columns.length;
            for (int i = 0; i < numColumns; i++) {
                qb.addField(columns[i]);
            }

            String[] orderByColumns = getOrderByColumns();
            if (orderByColumns != null) {
                for (int i = 0; i < orderByColumns.length; i++) {
                    qb.addOrderBy(orderByColumns[i]);
                }
            }

            // add in filters.... if applicable
            if (listID > 0 && filter != null) {
                filter.appendFilter(qb);
            }

            Query q = qb.executeQuery();
            List<Object[]> rs = (List<Object[]>) q.getResultList();

            String separator = getComboBoxTextDBColumnNamesSeparator();
            if (separator == null) {
                separator = " ";
            }

            for (Object[] resultRow : rs) {
                int id = (Integer) resultRow[0];

                int col = 1;
                String name = "";
                for (int i = 0; i < numColumns; i++, col++) {
                    name += resultRow[col];

                    // add the separator
                    if (i + 1 < numColumns) {
                        name += separator;
                    }
                }

                workingList.add(new DefaultListItem(id, name));
            }

            // cleanup
            em.close();
            qb.close();
        }
        return workingList;
    }

    /**
     * Finds the string name from the given id.  If none is found, an empty string is returned
     * @param id ID of the record to be found in the list
     * @return String of the name based on the column specified from getComboBoxTextDBColumnName()
     */
    public String getNameByItemID(int id) {
        String value = "";
        if (id < 0) {
            return value;
        }

        try {
            List<DefaultListItem> list = getList();
            for (DefaultListItem item : list) {
                if (id == item.getListID()) {
                    // found it!
                    value = item.getListValue().toString();
                    break;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return value;
    }

    private class TableGroup {

        private Component parent;
        private JPAEntityTable table;
        private List<JDBEntityColumnFilter> componentFilters;
        private EditableEntityListFilter filter;
        private boolean columnsAlreadyAdded = false;

        public TableGroup(JPAEntityTable table, Component parent, List<JDBEntityColumnFilter> componentFilters, EditableEntityListFilter filter) {
            this.setTable(table);
            this.setComponentFilters(componentFilters);
            this.setFilter(filter);

            if (parent == null) {
                throw new IllegalStateException("parent cannot be null");
            }

            if (parent instanceof Dialog || parent instanceof Frame) {
                this.setParent(parent);
            } else {
                throw new IllegalArgumentException("parent must be either a Dialog or Frame");
            }
        }

        public Component getParent() {
            return parent;
        }

        public void setParent(Component parent) {
            this.parent = parent;
        }

        public JPAEntityTable getTable() {
            return table;
        }

        public void setTable(JPAEntityTable table) {
            this.table = table;
        }

        public List<JDBEntityColumnFilter> getComponentFilters() {
            return componentFilters;
        }

        public void setComponentFilters(List<JDBEntityColumnFilter> componentFilters) {
            if (componentFilters != null) {
                this.componentFilters = componentFilters;
            } else {
                this.componentFilters = new ArrayList<JDBEntityColumnFilter>();
            }
        }

        public boolean isColumnsAlreadyAdded() {
            return columnsAlreadyAdded;
        }

        public void setColumnsAlreadyAdded(boolean columnsAlreadyAdded) {
            this.columnsAlreadyAdded = columnsAlreadyAdded;
        }

        public EditableEntityListFilter getFilter() {
            return filter;
        }

        public void setFilter(EditableEntityListFilter filter) {
            this.filter = filter;
        }
    }

    private class JLookupComboBoxGroup {

        private Component parent;
        private JLookupComboBox comboBox;
        private EditableEntityListFilter filter;
        //        public JLookupComboBoxGroup(JLookupComboBox comboBox, Component parent) {
//            this(comboBox, parent, null);
//        }

        public JLookupComboBoxGroup(JLookupComboBox comboBox, Component parent, EditableEntityListFilter filter) {
            this.setComboBox(comboBox);
            this.setFilter(filter);

            if (parent == null) {
                throw new IllegalStateException("parent cannot be null");
            }

            if (parent instanceof Dialog || parent instanceof Frame) {
                this.setParent(parent);
            } else {
                throw new IllegalArgumentException("parent must be either a Dialog or Frame");
            }
        }

        public boolean isFiltered() {
            return filter != null;
        }

        public JLookupComboBox getComboBox() {
            return comboBox;
        }

        public void setComboBox(JLookupComboBox comboBox) {
            this.comboBox = comboBox;
        }

        public Component getParent() {
            return parent;
        }

        public void setParent(Component parent) {
            this.parent = parent;
        }

        public EditableEntityListFilter getFilter() {
            return filter;
        }

        public void setFilter(EditableEntityListFilter filter) {
            this.filter = filter;
        }
    }

    public EntityItemEditor<T> getItemEditor() {
        return itemEditor;
    }

    public void setItemEditor(EntityItemEditor<T> itemEditor) {
        this.itemEditor = itemEditor;
    }

    public EntityItemViewer<T> getItemViewer() {
        return itemViewer;
    }

    public void setItemViewer(EntityItemViewer<T> itemViewer) {
        this.itemViewer = itemViewer;
    }
}