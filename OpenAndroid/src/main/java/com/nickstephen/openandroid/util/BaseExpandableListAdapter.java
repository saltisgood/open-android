package com.nickstephen.openandroid.util;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;

/**
 * A base class that implements the ExpandableListAdapter. Just given here for simplicity so I don't
 * have to implement everything if I don't want to. Also has some parameters for retrieving data
 * without having to cast the results. There is no type checking for these convenience methods though
 * so use at your own caution.
 * @param <T1> Group data type
 * @param <T2> Child data type
 */
public class BaseExpandableListAdapter<T1, T2> implements ExpandableListAdapter {
    /**
     * Indicates whether all the items in this adapter are enabled. If the value returned by this
     * method changes over time, there is no guarantee it will take effect. If true, it means
     * all items are selectable and clickable (there is no separator).
     * @return True if all items are enabled, false otherwise.
     */
    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    /**
     * Gets the data associated with the given child within the given group.
     * @param groupPosition the position of the group that the child resides in
     * @param childPosition the position of the child with respect to other children in the group
     * @return the data of the child
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    public T2 getChildData(int groupPosition, int childPosition) {
        //noinspection unchecked
        return (T2) getChild(groupPosition, childPosition);
    }

    /**
     * Gets the ID for the given child within the given group. This ID must be unique across
     * all children within the group. The combined ID must be unique across ALL items (groups
     * and all children).
     * @param groupPosition the position of the group that the child resides in
     * @param childPosition the position of the child with respect to other children in the group
     * @return the ID associated with the child
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     * Gets a View that displays the data for the given child within the given group
     * @param groupPosition the position fo the group that contains the child
     * @param childPosition the position of the child (for which the View is returned) within the group
     * @param isLastChild Whether the child is the last child within the group
     * @param convertView the old view to reuse, if possible. You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not
     *                    possible to convert this view to display the correct data, this
     *                    method can create a new view. It is not guaranteed that the convertView
     *                    will have been previously created by getChildView(...)
     * @param parent The parent that this view will eventually be attached to
     * @return the View corresponding to the child at the specified position
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return null;
    }

    /**
     * Gets the number of children in a specified group.
     * @param groupPosition the position of the group for which the children count should be returned
     * @return the children count in the specified group
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return 0;
    }

    /**
     * Gets an ID for a child that is unique across any item (either group or child) that is
     * in this list. Expandable lists require each item (group or child) to have a unique ID
     * among all children and groups in the list. This method is responsible for returning that
     * unique ID given a child's ID and its group's ID. Furthermore, if hasStableIds() is
     * true, the returned ID must be stable as well.
     * @param groupId The ID of the group that contains this child.
     * @param childId The ID of the child.
     * @return The unique (and possibly stable) ID of the child across all groups and children
     * in this list.
     */
    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return Integer.MAX_VALUE * groupId + childId + 1;
    }

    /**
     * Gets an ID for a group that is unique across any item (either group or child) that is
     * in this list. Expandable lists require each item (group or child) to have an unique ID
     * among all children and groups in the list. This method is responsible for returning
     * that unique ID given a group's ID. Furthermore, if hasStableIds() is true, the returned
     * ID must be stable as well.
     * @param groupId The ID of the group
     * @return The unique (and possibly stable) ID of the group across all groups and children
     * in this list.
     */
    @Override
    public long getCombinedGroupId(long groupId) {
        return Integer.MAX_VALUE * groupId;
    }

    /**
     * Gets the data associated with the given group.
     * @param groupPosition the position of the group
     * @return The data child for the specified group
     */
    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    public T1 getGroupData(int groupPosition) {
        //noinspection unchecked
        return (T1) getGroup(groupPosition);
    }

    /**
     * Gets the number of groups
     * @return The number of groups
     */
    @Override
    public int getGroupCount() {
        return 0;
    }

    /**
     * Gets the ID for the group at the given position. This group ID must be unique across
     * groups. The combined ID (see getCombinedGroupId(long)) must be unique across ALL items
     * (groups and all children).
     * @param groupPosition the position of the group for which the ID is wanted
     * @return The ID is associated with the group
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /**
     * Gets a View that displays the given group. This View is only for the group - the Views
     * for the group's children will be fetched using getChildView(...).
     * @param groupPosition The position of the group for which the View is returned
     * @param isExpanded whether the group is expanded or collapsed
     * @param convertView the old view to reuse, if possible. You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible
     *                    to convert this view to display the correct data, this method can
     *                    create a new view. It is not guaranteed that the convertView will
     *                    have been previously created by getGroupView(...).
     * @param parent the parent that this view will eventually be attached to
     * @return The View corresponding to the group at the specified position
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return null;
    }

    /**
     * Indicates whether the child and group IDs are stable across changes to the underlying
     * data.
     * @return whether or not the same ID always refers to the same object
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Whether the child at the specified position is selectable
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child within the group
     * @return whether the child is selectable
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * @return true if this adapter doesn't contain any data. This is used to determine whether
     * the empty view should be displayed. A typical implementation will return getCount() == 0
     * but since getCount() includes the headers and footers, specialized adapters might want
     * a different behaviour.
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Called when a group is collapsed
     * @param groupPosition The group being collapsed
     */
    @Override
    public void onGroupCollapsed(int groupPosition) {

    }

    /**
     * Called when a group is expanded
     * @param groupPosition The group being expanded
     */
    @Override
    public void onGroupExpanded(int groupPosition) {

    }

    /**
     * Register an observer that is called when changes happen to the data used by this adapter
     * @param dataSetObserver the object that gets notified when the data set changes
     */
    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
    }

    /**
     * Unregister an observer that has previously been registered with this adapter via
     * registerDataSetObserver(DataSetObserver).
     * @param dataSetObserver the object to unregister
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
    }
}
