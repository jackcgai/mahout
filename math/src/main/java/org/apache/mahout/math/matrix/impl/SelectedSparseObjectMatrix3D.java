/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
Copyright � 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
*/
package org.apache.mahout.math.matrix.impl;

import org.apache.mahout.math.map.AbstractIntObjectMap;
import org.apache.mahout.math.matrix.ObjectMatrix2D;
import org.apache.mahout.math.matrix.ObjectMatrix3D;

/**
 * Selection view on sparse 3-d matrices holding <tt>Object</tt> elements. First see the <a
 * href="package-summary.html">package summary</a> and javadoc <a href="package-tree.html">tree view</a> to get the
 * broad picture. <p> <b>Implementation:</b> <p> Objects of this class are typically constructed via
 * <tt>viewIndexes</tt> methods on some source matrix. The interface introduced in abstract super classes defines
 * everything a user can do. From a user point of view there is nothing special about this class; it presents the same
 * functionality with the same signatures and semantics as its abstract superclass(es) while introducing no additional
 * functionality. Thus, this class need not be visible to users. By the way, the same principle applies to concrete
 * DenseXXX and SparseXXX classes: they presents the same functionality with the same signatures and semantics as
 * abstract superclass(es) while introducing no additional functionality. Thus, they need not be visible to users,
 * either. Factory methods could hide all these concrete types. <p> This class uses no delegation. Its instances point
 * directly to the data. Cell addressing overhead is is 1 additional int addition and 3 additional array index accesses
 * per get/set. <p> Note that this implementation is not synchronized. <p> <b>Memory requirements:</b> <p> <tt>memory
 * [bytes] = 4*(sliceIndexes.length+rowIndexes.length+columnIndexes.length)</tt>. Thus, an index view with 100 x 100 x
 * 100 indexes additionally uses 8 KB. <p> <b>Time complexity:</b> <p> Depends on the parent view holding cells. <p>
 *
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
@SuppressWarnings("deprecation")
class SelectedSparseObjectMatrix3D<T> extends ObjectMatrix3D<T> {

  /** The elements of this matrix. */
  protected final AbstractIntObjectMap<T> elements;

  /** The offsets of the visible cells of this matrix. */
  protected int[] sliceOffsets;
  protected int[] rowOffsets;
  protected int[] columnOffsets;

  /** The offset. */
  protected int offset;

  /**
   * Constructs a matrix view with the given parameters.
   *
   * @param elements      the cells.
   * @param sliceOffsets  The slice offsets of the cells that shall be visible.
   * @param rowOffsets    The row offsets of the cells that shall be visible.
   * @param columnOffsets The column offsets of the cells that shall be visible.
   */
  protected SelectedSparseObjectMatrix3D(AbstractIntObjectMap<T> elements, int[] sliceOffsets, int[] rowOffsets,
                                         int[] columnOffsets, int offset) {
    // be sure parameters are valid, we do not check...
    int slices = sliceOffsets.length;
    int rows = rowOffsets.length;
    int columns = columnOffsets.length;
    setUp(slices, rows, columns);

    this.elements = elements;

    this.sliceOffsets = sliceOffsets;
    this.rowOffsets = rowOffsets;
    this.columnOffsets = columnOffsets;

    this.offset = offset;

    this.isNoView = false;
  }

  /**
   * Returns the position of the given absolute rank within the (virtual or non-virtual) internal 1-dimensional array.
   * Default implementation. Override, if necessary.
   *
   * @param absRank the absolute rank of the element.
   * @return the position.
   */
  @Override
  protected int _columnOffset(int absRank) {
    return columnOffsets[absRank];
  }

  /**
   * Returns the position of the given absolute rank within the (virtual or non-virtual) internal 1-dimensional array.
   * Default implementation. Override, if necessary.
   *
   * @param absRank the absolute rank of the element.
   * @return the position.
   */
  @Override
  protected int _rowOffset(int absRank) {
    return rowOffsets[absRank];
  }

  /**
   * Returns the position of the given absolute rank within the (virtual or non-virtual) internal 1-dimensional array.
   * Default implementation. Override, if necessary.
   *
   * @param absRank the absolute rank of the element.
   * @return the position.
   */
  @Override
  protected int _sliceOffset(int absRank) {
    return sliceOffsets[absRank];
  }

  /**
   * Returns the matrix cell value at coordinate <tt>[slice,row,column]</tt>.
   *
   * <p>Provided with invalid parameters this method may return invalid objects without throwing any exception. <b>You
   * should only use this method when you are absolutely sure that the coordinate is within bounds.</b> Precondition
   * (unchecked): <tt>slice&lt;0 || slice&gt;=slices() || row&lt;0 || row&gt;=rows() || column&lt;0 ||
   * column&gt;=column()</tt>.
   *
   * @param slice  the index of the slice-coordinate.
   * @param row    the index of the row-coordinate.
   * @param column the index of the column-coordinate.
   * @return the value at the specified coordinate.
   */
  @Override
  public T getQuick(int slice, int row, int column) {
    //if (debug) if (slice<0 || slice>=slices || row<0 || row>=rows || column<0 || column>=columns) throw new IndexOutOfBoundsException("slice:"+slice+", row:"+row+", column:"+column);
    //return elements.get(index(slice,row,column));
    //manually inlined:
    return elements.get(offset + sliceOffsets[sliceZero + slice * sliceStride] + rowOffsets[rowZero + row * rowStride] +
        columnOffsets[columnZero + column * columnStride]);
  }

  /**
   * Returns <tt>true</tt> if both matrices share common cells. More formally, returns <tt>true</tt> if <tt>other !=
   * null</tt> and at least one of the following conditions is met <ul> <li>the receiver is a view of the other matrix
   * <li>the other matrix is a view of the receiver <li><tt>this == other</tt> </ul>
   */
  @SuppressWarnings("unchecked")
  @Override
  protected boolean haveSharedCellsRaw(ObjectMatrix3D<T> other) {
    if (other instanceof SelectedSparseObjectMatrix3D) {
      SelectedSparseObjectMatrix3D otherMatrix = (SelectedSparseObjectMatrix3D) other;
      return this.elements == otherMatrix.elements;
    } else if (other instanceof SparseObjectMatrix3D) {
      SparseObjectMatrix3D otherMatrix = (SparseObjectMatrix3D) other;
      return this.elements == otherMatrix.elements;
    }
    return false;
  }

  /**
   * Returns the position of the given coordinate within the (virtual or non-virtual) internal 1-dimensional array.
   *
   * @param slice  the index of the slice-coordinate.
   * @param row    the index of the row-coordinate.
   * @param column the index of the third-coordinate.
   */
  @Override
  protected int index(int slice, int row, int column) {
    //return this.offset + super.index(slice,row,column);
    //manually inlined:
    return this.offset + sliceOffsets[sliceZero + slice * sliceStride] + rowOffsets[rowZero + row * rowStride] +
        columnOffsets[columnZero + column * columnStride];
  }

  /**
   * Construct and returns a new empty matrix <i>of the same dynamic type</i> as the receiver, having the specified
   * number of slices, rows and columns. For example, if the receiver is an instance of type
   * <tt>DenseObjectMatrix3D</tt> the new matrix must also be of type <tt>DenseObjectMatrix3D</tt>, if the receiver is
   * an instance of type <tt>SparseObjectMatrix3D</tt> the new matrix must also be of type
   * <tt>SparseObjectMatrix3D</tt>, etc. In general, the new matrix should have internal parametrization as similar as
   * possible.
   *
   * @param slices  the number of slices the matrix shall have.
   * @param rows    the number of rows the matrix shall have.
   * @param columns the number of columns the matrix shall have.
   * @return a new empty matrix of the same dynamic type.
   */
  @Override
  public ObjectMatrix3D<T> like(int slices, int rows, int columns) {
    return new SparseObjectMatrix3D<T>(slices, rows, columns);
  }

  /**
   * Construct and returns a new 2-d matrix <i>of the corresponding dynamic type</i>, sharing the same cells. For
   * example, if the receiver is an instance of type <tt>DenseObjectMatrix3D</tt> the new matrix must also be of type
   * <tt>DenseObjectMatrix2D</tt>, if the receiver is an instance of type <tt>SparseObjectMatrix3D</tt> the new matrix
   * must also be of type <tt>SparseObjectMatrix2D</tt>, etc.
   *
   * @param rows         the number of rows the matrix shall have.
   * @param columns      the number of columns the matrix shall have.
   * @param rowZero      the position of the first element.
   * @param columnZero   the position of the first element.
   * @param rowStride    the number of elements between two rows, i.e. <tt>index(i+1,j)-index(i,j)</tt>.
   * @param columnStride the number of elements between two columns, i.e. <tt>index(i,j+1)-index(i,j)</tt>.
   * @return a new matrix of the corresponding dynamic type.
   */
  @Override
  protected ObjectMatrix2D<T> like2D(int rows, int columns, int rowZero, int columnZero, int rowStride, int columnStride) {
    throw new InternalError(); // this method is never called since viewRow() and viewColumn are overridden properly.
  }

  /**
   * Sets the matrix cell at coordinate <tt>[slice,row,column]</tt> to the specified value.
   *
   * <p>Provided with invalid parameters this method may access illegal indexes without throwing any exception. <b>You
   * should only use this method when you are absolutely sure that the coordinate is within bounds.</b> Precondition
   * (unchecked): <tt>slice&lt;0 || slice&gt;=slices() || row&lt;0 || row&gt;=rows() || column&lt;0 ||
   * column&gt;=column()</tt>.
   *
   * @param slice  the index of the slice-coordinate.
   * @param row    the index of the row-coordinate.
   * @param column the index of the column-coordinate.
   * @param value  the value to be filled into the specified cell.
   */
  @Override
  public void setQuick(int slice, int row, int column, T value) {
    //if (debug) if (slice<0 || slice>=slices || row<0 || row>=rows || column<0 || column>=columns) throw new IndexOutOfBoundsException("slice:"+slice+", row:"+row+", column:"+column);
    //int index =  index(slice,row,column);
    //manually inlined:
    int index = offset + sliceOffsets[sliceZero + slice * sliceStride] + rowOffsets[rowZero + row * rowStride] +
        columnOffsets[columnZero + column * columnStride];
    if (value == null) {
      this.elements.removeKey(index);
    } else {
      this.elements.put(index, value);
    }
  }

  /**
   * Sets up a matrix with a given number of slices and rows.
   *
   * @param slices  the number of slices the matrix shall have.
   * @param rows    the number of rows the matrix shall have.
   * @param columns the number of columns the matrix shall have.
   * @throws IllegalArgumentException if <tt>(Object)rows*slices > Integer.MAX_VALUE</tt>.
   */
  @Override
  protected void setUp(int slices, int rows, int columns) {
    super.setUp(slices, rows, columns);
    this.sliceStride = 1;
    this.rowStride = 1;
    this.columnStride = 1;
    this.offset = 0;
  }

  /**
   * Self modifying version of viewDice().
   *
   * @throws IllegalArgumentException if some of the parameters are equal or not in range 0..2.
   */
  @Override
  protected AbstractMatrix3D vDice(int axis0, int axis1, int axis2) {
    super.vDice(axis0, axis1, axis2);

    // swap offsets
    int[][] offsets = new int[3][];
    offsets[0] = this.sliceOffsets;
    offsets[1] = this.rowOffsets;
    offsets[2] = this.columnOffsets;

    this.sliceOffsets = offsets[axis0];
    this.rowOffsets = offsets[axis1];
    this.columnOffsets = offsets[axis2];

    return this;
  }

  /**
   * Constructs and returns a new 2-dimensional <i>slice view</i> representing the slices and rows of the given column.
   * The returned view is backed by this matrix, so changes in the returned view are reflected in this matrix, and
   * vice-versa. <p> To obtain a slice view on subranges, construct a sub-ranging view (<tt>view().part(...)</tt>), then
   * apply this method to the sub-range view. To obtain 1-dimensional views, apply this method, then apply another slice
   * view (methods <tt>viewColumn</tt>, <tt>viewRow</tt>) on the intermediate 2-dimensional view. To obtain
   * 1-dimensional views on subranges, apply both steps.
   *
   * @param column the index of the column to fix.
   * @return a new 2-dimensional slice view.
   * @throws IndexOutOfBoundsException if <tt>column < 0 || column >= columns()</tt>.
   * @see #viewSlice(int)
   * @see #viewRow(int)
   */
  @Override
  public ObjectMatrix2D<T> viewColumn(int column) {
    checkColumn(column);

    int viewRows = this.slices;
    int viewColumns = this.rows;

    int viewRowZero = sliceZero;
    int viewColumnZero = rowZero;
    int viewOffset = this.offset + _columnOffset(_columnRank(column));

    int viewRowStride = this.sliceStride;
    int viewColumnStride = this.rowStride;

    int[] viewRowOffsets = this.sliceOffsets;
    int[] viewColumnOffsets = this.rowOffsets;

    return new SelectedSparseObjectMatrix2D<T>(viewRows, viewColumns, this.elements, viewRowZero, viewColumnZero,
        viewRowStride, viewColumnStride, viewRowOffsets, viewColumnOffsets, viewOffset);
  }

  /**
   * Constructs and returns a new 2-dimensional <i>slice view</i> representing the slices and columns of the given row.
   * The returned view is backed by this matrix, so changes in the returned view are reflected in this matrix, and
   * vice-versa. <p> To obtain a slice view on subranges, construct a sub-ranging view (<tt>view().part(...)</tt>), then
   * apply this method to the sub-range view. To obtain 1-dimensional views, apply this method, then apply another slice
   * view (methods <tt>viewColumn</tt>, <tt>viewRow</tt>) on the intermediate 2-dimensional view. To obtain
   * 1-dimensional views on subranges, apply both steps.
   *
   * @param row the index of the row to fix.
   * @return a new 2-dimensional slice view.
   * @throws IndexOutOfBoundsException if <tt>row < 0 || row >= row()</tt>.
   * @see #viewSlice(int)
   * @see #viewColumn(int)
   */
  @Override
  public ObjectMatrix2D<T> viewRow(int row) {
    checkRow(row);

    int viewRows = this.slices;
    int viewColumns = this.columns;

    int viewRowZero = sliceZero;
    int viewColumnZero = columnZero;
    int viewOffset = this.offset + _rowOffset(_rowRank(row));

    int viewRowStride = this.sliceStride;
    int viewColumnStride = this.columnStride;

    int[] viewRowOffsets = this.sliceOffsets;
    int[] viewColumnOffsets = this.columnOffsets;

    return new SelectedSparseObjectMatrix2D<T>(viewRows, viewColumns, this.elements, viewRowZero, viewColumnZero,
        viewRowStride, viewColumnStride, viewRowOffsets, viewColumnOffsets, viewOffset);
  }

  /**
   * Construct and returns a new selection view.
   *
   * @param sliceOffsets  the offsets of the visible elements.
   * @param rowOffsets    the offsets of the visible elements.
   * @param columnOffsets the offsets of the visible elements.
   * @return a new view.
   */
  @Override
  protected ObjectMatrix3D<T> viewSelectionLike(int[] sliceOffsets, int[] rowOffsets, int[] columnOffsets) {
    return new SelectedSparseObjectMatrix3D<T>(this.elements, sliceOffsets, rowOffsets, columnOffsets, this.offset);
  }

  /**
   * Constructs and returns a new 2-dimensional <i>slice view</i> representing the rows and columns of the given slice.
   * The returned view is backed by this matrix, so changes in the returned view are reflected in this matrix, and
   * vice-versa. <p> To obtain a slice view on subranges, construct a sub-ranging view (<tt>view().part(...)</tt>), then
   * apply this method to the sub-range view. To obtain 1-dimensional views, apply this method, then apply another slice
   * view (methods <tt>viewColumn</tt>, <tt>viewRow</tt>) on the intermediate 2-dimensional view. To obtain
   * 1-dimensional views on subranges, apply both steps.
   *
   * @param slice the index of the slice to fix.
   * @return a new 2-dimensional slice view.
   * @throws IndexOutOfBoundsException if <tt>slice < 0 || slice >= slices()</tt>.
   * @see #viewRow(int)
   * @see #viewColumn(int)
   */
  @Override
  public ObjectMatrix2D<T> viewSlice(int slice) {
    checkSlice(slice);

    int viewRows = this.rows;
    int viewColumns = this.columns;

    int viewRowZero = rowZero;
    int viewColumnZero = columnZero;
    int viewOffset = this.offset + _sliceOffset(_sliceRank(slice));

    int viewRowStride = this.rowStride;
    int viewColumnStride = this.columnStride;

    int[] viewRowOffsets = this.rowOffsets;
    int[] viewColumnOffsets = this.columnOffsets;

    return new SelectedSparseObjectMatrix2D<T>(viewRows, viewColumns, this.elements, viewRowZero, viewColumnZero,
        viewRowStride, viewColumnStride, viewRowOffsets, viewColumnOffsets, viewOffset);
  }
}
