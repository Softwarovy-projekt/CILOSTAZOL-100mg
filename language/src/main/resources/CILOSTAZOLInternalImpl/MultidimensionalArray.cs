using System.Collections;

namespace CILOSTAZOLInternalImpl;

public class MultidimensionalArray<T> : ICloneable, IList, IStructuralComparable, IStructuralEquatable
{
    private readonly T[] _array;
    private int[] _lengths;
    private readonly int _rank;
    private readonly int _length;

    #region Array

    public bool IsSynchronized => false;
    public object SyncRoot => this;
    public bool IsFixedSize => true;
    public bool IsReadOnly => false;
    public int Length => _length;
    public long LongLength => _length;

    public int Rank => _rank;

    public MultidimensionalArray(int length1, int length2)
    {
        _length = length1 * length2;
        _array = new T[_length];
        _lengths = new int[] { length1, length2 };
        _rank = 2;
    }

    public MultidimensionalArray(int length1, int length2, int length3)
    {
        _length = length1 * length2 * length3;
        _array = new T[_length];
        _lengths = new int[] { length1, length2, length3 };
        _rank = 3;
    }

    public MultidimensionalArray(int[] lengths)
    {
        _length = 1;
        foreach (var length in lengths)
        {
            _length *= length;
        }

        _array = new T[_length];
        _lengths = lengths;
        _rank = lengths.Length;
    }

    public T Get(int index1, int index2)
    {
        if (_rank != 2)
        {
            throw new ArgumentException("Need 2D array.");
        }

        return _array[GetFlatIndex(index1, index2)];
    }

    public T Get(int index1, int index2, int index3)
    {
        if (_rank != 3)
        {
            throw new ArgumentException("Need 3D array.");
        }

        return _array[GetFlatIndex(index1, index2, index3)];
    }

    public T Get(int[] indices)
    {
        if (indices.Length != _rank)
        {
            throw new ArgumentException("Wrong number of indices.");
        }

        var index = GetFlatIndex(indices);

        return _array[index];
    }

    public void Set(int index1, int index2, T value)
    {
        if (_rank != 2)
        {
            throw new ArgumentException("Need 2D array.");
        }

        _array[GetFlatIndex(index1, index2)] = value;
    }

    public void Set(int index1, int index2, int index3, T value)
    {
        if (_rank != 3)
        {
            throw new ArgumentException("Need 3D array.");
        }

        _array[GetFlatIndex(index1, index2, index3)] = value;
    }

    public void Set(int[] indices, T value)
    {
        if (indices.Length != _rank)
        {
            throw new ArgumentException("Wrong number of indices.");
        }

        _array[GetFlatIndex(indices)] = value;
    }

    public T Get(long index1, long index2)
    {
        int iindex1 = (int)index1;
        int iindex2 = (int)index2;
        if (index1 != iindex1)
            throw new ArgumentOutOfRangeException("Huge array not supported.");
        if (index2 != iindex2)
            throw new ArgumentOutOfRangeException("Huge array not supported.");

        return Get(iindex1, iindex2);
    }

    public T Get(long index1, long index2, long index3)
    {
        int iindex1 = (int)index1;
        int iindex2 = (int)index2;
        int iindex3 = (int)index3;
        if (index1 != iindex1)
            throw new ArgumentOutOfRangeException("Huge array not supported.");
        if (index2 != iindex2)
            throw new ArgumentOutOfRangeException("Huge array not supported.");
        if (index3 != iindex3)
            throw new ArgumentOutOfRangeException("Huge array not supported.");

        return Get(iindex1, iindex2, iindex3);
    }

    public T Get(params long[] indices)
    {
        if (indices == null)
            throw new ArgumentNullException(nameof(indices));
        if (Rank != indices.Length)
            throw new ArgumentException("Wrong number of indices.");

        int[] intIndices = new int[indices.Length];

        for (int i = 0; i < indices.Length; ++i)
        {
            long index = indices[i];
            int iindex = (int)index;
            if (index != iindex)
                throw new ArgumentOutOfRangeException("Huge array not supported.");
            ;
            intIndices[i] = iindex;
        }

        return Get(intIndices);
    }

    public void Set(long index1, long index2, T value)
    {
        int iindex1 = (int)index1;
        int iindex2 = (int)index2;
        if (index1 != iindex1)
            throw new ArgumentOutOfRangeException("Huge array not supported.");
        if (index2 != iindex2)
            throw new ArgumentOutOfRangeException("Huge array not supported.");

        Set(iindex1, iindex2, value);
    }

    public void Set(long index1, long index2, long index3, T value)
    {
        int iindex1 = (int)index1;
        int iindex2 = (int)index2;
        int iindex3 = (int)index3;
        if (index1 != iindex1)
            throw new ArgumentOutOfRangeException("Huge array not supported.");
        if (index2 != iindex2)
            throw new ArgumentOutOfRangeException("Huge array not supported.");
        if (index3 != iindex3)
            throw new ArgumentOutOfRangeException("Huge array not supported.");

        Set(iindex1, iindex2, iindex3, value);
    }

    public void Set(long[] indices, T value)
    {
        if (indices == null)
            throw new ArgumentNullException(nameof(indices));
        if (Rank != indices.Length)
            throw new ArgumentException("Wrong number of indices.");

        int[] intIndices = new int[indices.Length];

        for (int i = 0; i < indices.Length; ++i)
        {
            long index = indices[i];
            int iindex = (int)index;
            if (index != iindex)
                throw new ArgumentOutOfRangeException("Huge array not supported.");
            ;
            intIndices[i] = iindex;
        }

        Set(intIndices, value);
    }


    public int GetLength(int dimension)
    {
        if (dimension < 0 || dimension >= _rank)
        {
            throw new IndexOutOfRangeException();
        }

        return _lengths[dimension];
    }

    public long GetLongLength(int dimension)
    {
        return GetLength(dimension);
    }

    public int GetLowerBound(int dimension)
    {
        if (dimension < 0 || dimension >= _rank)
        {
            throw new IndexOutOfRangeException();
        }

        return 0;
    }

    public int GetUpperBound(int dimension)
    {
        if (dimension < 0 || dimension >= _rank)
        {
            throw new IndexOutOfRangeException();
        }

        return _lengths[dimension] - 1;
    }

    public void Initialize()
    {
        for (int i = 0; i < _length; i++)
        {
            _array[i] = default!;
        }
    }
    
    public void CopyTo(Array array, long index)
    {
        throw new NotSupportedException("Multidimensional array.");
    }

    #endregion

    #region ICloneable

    public object Clone()
    {
        var copy =  new MultidimensionalArray<T>(_lengths);
        for (int i = 0; i < _length; i++)
        {
            copy._array[i] = _array[i];
        }

        return copy;
    }

    #endregion

    #region IList

    int ICollection.Count => _array.Length;

    public IEnumerator GetEnumerator()
    {
        return _array.GetEnumerator();
    }

    public void CopyTo(Array array, int index)
    {
        throw new NotSupportedException("Multidimensional array.");
    }
    

    int IList.Add(object? value)
    {
        throw new NotSupportedException("Fixed size collection.");
    }

    void IList.Clear()
    {
        for (int i = 0; i < _array.Length; i++)
        {
            _array[i] = default!;
        }
    }

    bool IList.Contains(object? value)
    {
        return value is T tValue && _array.Contains(tValue);
    }

    int IList.IndexOf(object? value)
    {
        throw new NotSupportedException("Multidimensional array.");
    }

    void IList.Insert(int index, object? value)
    {
        throw new NotSupportedException("Fixed size collection.");
    }

    void IList.Remove(object? value)
    {
        throw new NotSupportedException("Fixed size collection.");
    }

    void IList.RemoveAt(int index)
    {
        throw new NotSupportedException("Fixed size collection.");
    }

    object? IList.this[int index]
    {
        get => throw new NotSupportedException("Multidimensional array.");
        set => throw new NotSupportedException("Multidimensional array.");
    }

    #endregion

    #region IStructuralComparable

    int IStructuralComparable.CompareTo(object? other, IComparer comparer)
    {
        if (other == null)
        {
            return 1;
        }

        MultidimensionalArray<T>? o = other as MultidimensionalArray<T>;

        if (o == null || this.Length != o.Length)
        {
            throw new ArgumentException("Other of different type or length.");        
        }

        int i = 0;
        int c = 0;

        while (i < o.Length && c == 0)
        {
            object? left = _array[i];
            object? right = o._array[i];

            c = comparer.Compare(left, right);
            i++;
        }

        return c;
    }

    #endregion
    
    #region IStructuralEquatable
    
    bool IStructuralEquatable.Equals(object? other, IEqualityComparer comparer)
    {
        if (other == null)
        {
            return false;
        }

        if (object.ReferenceEquals(this, other))
        {
            return true;
        }

        if (!(other is MultidimensionalArray<T> o) || o.Length != this.Length)
        {
            return false;
        }

        int i = 0;
        while (i < o.Length)
        {
            T left = _array[i];
            T right = o._array[i];

            if (!comparer.Equals(left, right))
            {
                return false;
            }
            i++;
        }

        return true;
    }

    int IStructuralEquatable.GetHashCode(IEqualityComparer comparer)
    {
        if (comparer == null)
            throw new ArgumentNullException(nameof(comparer));

        HashCode hashCode = default;

        for (int i = (this.Length >= 8 ? this.Length - 8 : 0); i < this.Length; i++)
        {
            hashCode.Add(comparer.GetHashCode(_array[i]!));
        }

        return hashCode.ToHashCode();
    }

    #endregion

    #region private helpers

    private int GetFlatIndex(int index1, int index2)
    {
        return index1 * _lengths[0] + index2;
    }

    private int GetFlatIndex(int index1, int index2, int index3)
    {
        return index1 * _lengths[0] + index2 * _lengths[1] + index3;
    }

    private int GetFlatIndex(int[] indices)
    {
        int index = 0;
        for (int i = 0; i < _rank - 1; i++)
        {
            index += indices[i] * _lengths[i];
        }
        
        return index + indices[_rank - 1];
    }

    #endregion
}