using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using Sharp2D;

namespace GhostClient.Core
{
    public interface ISpriteWorld
    {
        void AddSprite(Sprite sprite);

        void RemoveSprite(Sprite sprite);

        List<Sprite> GetSprites { get; } 
    }
}
