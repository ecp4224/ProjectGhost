using Microsoft.Xna.Framework;

namespace Sharp2D.Core.Interfaces
{
    public interface IMoveable3d : IMoveable2d
    {
        float Z { get; set; }

        Vector3 Vector3d { get; set; } 
    }
}
