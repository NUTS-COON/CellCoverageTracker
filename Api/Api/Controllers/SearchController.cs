using Api.Models;
using Api.Services.Interfaces;
using Microsoft.AspNetCore.Cors;
using Microsoft.AspNetCore.Mvc;
using System.Collections.Generic;
using System.Threading.Tasks;
using Api.Logic;
using System.Linq;
using System;

namespace Api.Controllers
{
    [Route("api/[controller]/[action]")]
    [EnableCors("AllowAll")]
    [ApiController]
    public class SearchController : ControllerBase
    {
        private readonly IRouteSearcher _routeSearcher;
        private readonly IHereService _hereService;
        private readonly DataService _dataService;

        public SearchController(IRouteSearcher routeSearcher, IHereService hereService,
            DataService dataService)
        {
            _routeSearcher = routeSearcher;
            _hereService = hereService;
            _dataService = dataService;
        }

        [HttpPost]
        public async Task<RouteInfoResponse> RouteInfo([FromBody]RouteSearchInfo model)
        {
            var route = await _routeSearcher.GetHereRoutes(model.From, model.To);
            if (route == null)
                return null;
            
            var points = await _dataService.SearchGeo(route.ToArray());

            var distinctAveragePoints = points.GroupBy(c => c, new CellPointComparer())
                .Select(g => new CellPoint
                {
                    Level = (int)Math.Round(g.Average(c => c.Level)),
                    Latitude = g.Key.Latitude,
                    Longitude = g.Key.Longitude,
                    CellType = g.Key.CellType,
                    OperatorName = g.Key.OperatorName
                });

            return new RouteInfoResponse()
            {
                Route = route,
                Points = distinctAveragePoints.Take(500).ToList(),
                Stat = new RouteStat()
                {
                    OperatorStats = new List<RouteOperatorStat>()
                    {
                        new RouteOperatorStat()
                        {
                            OperatorName = "MTS",
                            PercentNone = 0,
                            Percent2G = 10,
                            Percent3G = 30,
                            Percent4G = 60,
                        },
                        new RouteOperatorStat()
                        {
                            OperatorName = "Beeline",
                            PercentNone = 10,
                            Percent2G = 20,
                            Percent3G = 30,
                            Percent4G = 40,
                        }
                    }
                }
            };
        }

        [HttpPost]
        public async Task<IEnumerable<SuggesionAddress>> GetSuggestionsWithCoordinates([FromBody]AddressText model)
        {
            return await _hereService.GetSuggestionsWithCoordinates(model?.Text);
        }

        [HttpPost]
        public async Task<IEnumerable<SuggesionAddress>> GetSuggestions([FromBody]AddressText model)
        {
            var suggestions = await Task.WhenAll(_hereService.GetSuggestions(model?.Text), _hereService.GetPlacesSuggestion(model?.Text));
            return suggestions[0].Concat(suggestions[1]);
        }

        [HttpPost]
        public async Task<IEnumerable<SuggesionAddress>> GetPlacesSuggestions([FromBody]AddressText model)
        {
            return await _hereService.GetPlacesSuggestion(model?.Text);
        }

        [HttpPost]
        public async Task<Coordinate> GetCoordinate([FromBody]LocationIdModel model)
        {
            return await _hereService.GetCoordinate(model?.LocationId);
        }

        [HttpPost]
        public async Task<object> GetPoints([FromBody]RectangleOfSearch model)
        {
            return await _dataService.SearchGeo(new [] { model.LeftBottom, model.RightTop });
        }
    }
}