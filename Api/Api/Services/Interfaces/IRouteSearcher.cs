using Api.Models;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Api.Services.Interfaces
{
    public interface IRouteSearcher
    {
        Task<List<TargetRoute>> GetHereRoutes(CoordinateOrLocation from, CoordinateOrLocation to, DateTime time, bool allowPedestrian = false);
    }
}
