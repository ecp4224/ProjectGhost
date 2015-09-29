using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;

namespace Ghost.Core
{
    /// <summary>
    /// This class represents a numerical stat that can have temporary buffs applied to it's value to alter the value returned.
    /// The actual value of this stat is called the TrueValue, and the computed value is the Value. The TrueValue of a stat should not be changed
    /// unless it's considered a perminate change, as this will result in unexpected results when many Buffs are applied.
    /// </summary>
    public sealed class Stat : IEquatable<Stat>, ICloneable
    {
        [Flags]
        public enum BuffType
        {
            Addition = 1,
            Subtraction = 2,
            Percentage = 4,
            AddPercent = Addition | Percentage,
            SubtractPercent = Subtraction | Percentage
        }

        public struct Buff
        {
            public BuffType Type;
            public double BuffValue;
            public string Name;
        }

        private bool _dirty = true;
        private double _trueValue;
        private double _cachedValue;
        private readonly object _valueLock = new object();
        private readonly List<Buff> _buffs = new List<Buff>();

        public IEnumerable<Buff> Buffs
        {
            get { return new ReadOnlyCollection<Buff>(_buffs); }
        }

        public double TrueValue
        {
            get { return _trueValue; }
            set
            {
                _trueValue = value;
                _dirty = true;
            }
        }

        public double Value
        {
            get
            {
                lock (_valueLock)
                {
                    if (!_dirty) return _cachedValue;

                    _cachedValue = TrueValue;
                    foreach (Buff buff in _buffs)
                    {
                        bool percent = (buff.Type & BuffType.Percentage) != 0;
                        if ((buff.Type & BuffType.Subtraction) != 0)
                        {
                            if (percent)
                            {
                                _cachedValue -= _cachedValue * (buff.BuffValue / 100.0);
                            }
                            else
                            {
                                _cachedValue -= buff.BuffValue;
                            }
                        }
                        else
                        {
                            if (percent)
                            {
                                _cachedValue += _cachedValue * (buff.BuffValue / 100.0);
                            }
                            else
                            {
                                _cachedValue += buff.BuffValue;
                            }
                        }
                    }

                    _dirty = false;
                }

                return _cachedValue;
            }
        }

        public Stat(Stat stat)
        {
            TrueValue = stat.TrueValue;
            _buffs = stat._buffs;
        }

        public Stat(double trueValue)
        {
            TrueValue = trueValue;
        }

        public Stat()
            : this(0.0)
        {
        }

        #region Operator Override
        public static implicit operator Stat(double value)
        {
            return new Stat(value);
        }

        public static implicit operator Stat(float value)
        {
            return new Stat(value);
        }

        public static implicit operator Stat(int value)
        {
            return new Stat(value);
        }

        public static Stat operator +(Stat stat, double value)
        {
            stat.TrueValue += value;
            return stat;
        }

        public static Stat operator -(Stat stat, double value)
        {
            stat.TrueValue -= value;
            return stat;
        }

        public static Stat operator *(Stat stat, double value)
        {
            stat.TrueValue *= value;
            return stat;
        }

        public static Stat operator /(Stat stat, double value)
        {
            stat.TrueValue /= value;
            return stat;
        }

        public static Stat operator +(Stat stat1, Stat stat2)
        {
            var toReturn = new Stat { TrueValue = stat1.TrueValue + stat2.TrueValue };
            toReturn._buffs.AddRange(stat1._buffs.Union(stat2._buffs));

            return toReturn;
        }

        public static Stat operator +(Stat stat, float value)
        {
            stat.TrueValue += value;
            return stat;
        }

        public static Stat operator -(Stat stat, float value)
        {
            stat.TrueValue -= value;
            return stat;
        }

        public static Stat operator *(Stat stat, float value)
        {
            stat.TrueValue *= value;
            return stat;
        }

        public static Stat operator /(Stat stat, float value)
        {
            stat.TrueValue /= value;
            return stat;
        }
        public static Stat operator +(Stat stat, int value)
        {
            stat.TrueValue += value;
            return stat;
        }

        public static Stat operator -(Stat stat, int value)
        {
            stat.TrueValue -= value;
            return stat;
        }

        public static Stat operator *(Stat stat, int value)
        {
            stat.TrueValue *= value;
            return stat;
        }

        public static Stat operator /(Stat stat, int value)
        {
            stat.TrueValue /= value;
            return stat;
        }

        public static bool operator ==(Stat stat, int value)
        {
            return stat != null && stat.Value == value;
        }

        public static bool operator !=(Stat stat, int value)
        {
            return stat == null || stat.Value != value;
        }

        public static bool operator >(Stat stat, int value)
        {
            return stat != null && stat.Value > value;
        }

        public static bool operator <(Stat stat, int value)
        {
            return stat == null || stat.Value < value;
        }

        public static bool operator >=(Stat stat, int value)
        {
            return stat != null && stat.Value >= value;
        }

        public static bool operator <=(Stat stat, int value)
        {
            return stat == null || stat.Value <= value;
        }

        public static bool operator ==(Stat stat, double value)
        {
            return stat != null && stat.Value == value;
        }

        public static bool operator !=(Stat stat, double value)
        {
            return stat == null || stat.Value != value;
        }

        public static bool operator >(Stat stat, double value)
        {
            return stat != null && stat.Value > value;
        }

        public static bool operator <(Stat stat, double value)
        {
            return stat == null || stat.Value < value;
        }

        public static bool operator >=(Stat stat, double value)
        {
            return stat != null && stat.Value >= value;
        }

        public static bool operator <=(Stat stat, double value)
        {
            return stat == null || stat.Value <= value;
        }

        public static bool operator ==(Stat stat, float value)
        {
            return stat != null && stat.Value == value;
        }

        public static bool operator !=(Stat stat, float value)
        {
            return stat == null || stat.Value != value;
        }

        public static bool operator >(Stat stat, float value)
        {
            return stat != null && stat.Value > value;
        }

        public static bool operator <(Stat stat, float value)
        {
            return stat == null || stat.Value < value;
        }

        public static bool operator >=(Stat stat, float value)
        {
            return stat != null && stat.Value >= value;
        }

        public static bool operator <=(Stat stat, float value)
        {
            return stat == null || stat.Value <= value;
        }



        public bool Equals(Stat other)
        {
            if (ReferenceEquals(null, other)) return false;
            if (ReferenceEquals(this, other)) return true;
            return Equals(_buffs, other._buffs) && TrueValue.Equals(other.TrueValue);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                return ((_buffs != null ? _buffs.GetHashCode() : 0) * 397) ^ TrueValue.GetHashCode();
            }
        }

        public object Clone()
        {
            return new Stat(this);
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj is Stat && Equals((Stat)obj);
        }

        public override string ToString()
        {
            return Value.ToString();
        }

        #endregion

        public Buff AddBuff(BuffType type, double buffValue, string name, bool stack = true)
        {
            _dirty = true;

            if (!stack)
            {
                Buff temp = _buffs.FirstOrDefault(b => b.Name == name);
                if (!EqualityComparer<Buff>.Default.Equals(temp, default(Buff)))
                {
                    temp.Type = type;
                    temp.BuffValue = buffValue;
                    return temp;
                }
            }

            var buff = new Buff { Type = type, BuffValue = buffValue, Name = name };
            _buffs.Add(buff);
            return buff;
        }

        public Buff AddBuff(Buff buff)
        {
            if (_buffs.Contains(buff)) return buff;
            _dirty = true;
            _buffs.Add(buff);
            return buff;
        }

        public void RemoveBuff(Buff buff)
        {
            if (!_buffs.Contains(buff)) return;
            _dirty = true;
            _buffs.Remove(buff);
        }

        public void RemoveBuff(string name)
        {
            _dirty = true;

            _buffs.RemoveAll(b => b.Name == name);
        }

        public void ClearBuffs()
        {
            _dirty = true;

            _buffs.Clear();
        }
    }
}
