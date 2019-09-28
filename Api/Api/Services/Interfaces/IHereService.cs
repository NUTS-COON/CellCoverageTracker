using Api.Models;
using System;
using System.Threading.Tasks;

namespace Api.Services.Interfaces
{
    public interface IHereService
    {
        Task<HereRouteResponse> GetRoutes(DateTime time, Coordinate from, Coordinate to, string mode = "publicTransport");
    }
}
