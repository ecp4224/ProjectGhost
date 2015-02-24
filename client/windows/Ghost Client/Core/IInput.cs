using OpenTK;

namespace Ghost.Core
{
    public interface IInput
    {
        bool IsConnected { get; }

        Vector2 CalculateMovement();

        bool CheckInputFor(Player player);

        bool Equals(IInput input);
    }
}
