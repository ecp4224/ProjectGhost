using Ghost.Core;
using OpenTK.Input;
using Sharp2D;

namespace Ghost
{
    public class InputPlayer : Player
    {
        
        private readonly IInput _input;
        public bool HasController
        {
            get { return _input.IsConnected; }
        }

        public IInput Input
        {
            get { return _input; }
        }

        public InputPlayer(int playerNumber, IInput input) : base(playerNumber)
        {
            _input = input;
        }

        protected override void OnLoad()
        {
            base.OnLoad();

            Texture = Texture.NewTexture("sprites/ball.png");
            Texture.LoadTextureFromFile();

            Width = Texture.TextureWidth;
            Height = Texture.TextureHeight;
        }

        protected override void OnInput()
        {
            var movementVector = _input.CalculateMovement();

            XVel = movementVector.X * 7f;
            YVel = movementVector.Y * 7f;

            Logger.Debug(X + " : " + Y);
        }
    }
}
