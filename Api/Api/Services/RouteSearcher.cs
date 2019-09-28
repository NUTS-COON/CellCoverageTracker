using Api.Models;
using Api.Services.Interfaces;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Threading.Tasks;

namespace Api.Services
{
    public class RouteSearcher : IRouteSearcher
    {
        private readonly IHereService _hereService;

        public RouteSearcher(IHereService hereService)
        {
            _hereService = hereService;
        }

        public async Task<List<Coordinate>> GetHereRoutes(CoordinateOrLocation fromLocation, CoordinateOrLocation toLocation)
        {
            var from = await GetCoordinate(fromLocation);
            var to = await GetCoordinate(toLocation);

            var result = await _hereService.GetRoutes(DateTime.Now, from, to);
            if (result == null)
                return null;

            var coords = result.Response.Route.FirstOrDefault()?.Shape;
            if (coords == null)
                return null;

            
            var nfi = new CultureInfo( "en-US", false ).NumberFormat;
            nfi.NumberDecimalSeparator = ".";
            
            return coords
                .Select(c =>
                {
                    var pair = c.Split(',');
                    return new Coordinate()
                    {
                        Latitude = double.Parse(pair[0], nfi),
                        Longitude = double.Parse(pair[1], nfi)
                    };
                })
                .ToList();
        }

        private async Task<Coordinate> GetCoordinate(CoordinateOrLocation coordinateOrLocation)
        {
            if (coordinateOrLocation.Coordinate != null)
                return coordinateOrLocation.Coordinate;

            return await _hereService.GetCoordinate(coordinateOrLocation.LocationId);
        }
    }
}
