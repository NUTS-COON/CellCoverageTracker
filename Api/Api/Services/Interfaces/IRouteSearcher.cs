using Api.Models;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Api.Services.Interfaces
{
    public interface IRouteSearcher
    {
        Task<List<Coordinate>> GetHereRoutes(CoordinateOrLocation from, CoordinateOrLocation to);
    }
}
