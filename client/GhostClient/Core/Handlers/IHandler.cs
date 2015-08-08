namespace Ghost.Core.Handlers
{
    public interface IHandler
    {
        void Start();

        void Tick();
    }
}
