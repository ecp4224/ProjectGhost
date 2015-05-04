using System.Collections.Generic;
using Microsoft.Xna.Framework.Graphics;

namespace Sharp2D.Core.Interfaces
{
    public interface ILogicContainer
    {
        List<ILogical> LogicalList { get; } //Fetch
        void AddLogical(ILogical logical); //Add a logical to the LogicalList
        void RemoveLogical(ILogical logical); //Remove a logical from the LogicalList
    }
}
