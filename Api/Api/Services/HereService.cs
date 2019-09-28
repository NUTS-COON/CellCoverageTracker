using Api.Helpers;
using Api.Models;
using Api.Services.Interfaces;
using Api.Settings;
using System;
using System.Globalization;
using System.Text;
using System.Threading.Tasks;

namespace Api.Services
{
    public class HereService : IHereService
    {
        private readonly HereSettings _hereSettings;

        public HereService(HereSettings hereSettings)
        {
            _hereSettings = hereSettings;
        }

        private static NumberFormatInfo NFI = new NumberFormatInfo { NumberDecimalSeparator = "." };

        public async Task<HereRouteResponse> GetRoutes(DateTime time, Coordinate from, Coordinate to, string mode = "publicTransport")
        {
            var url = new StringBuilder()
                .Append("https://route.api.here.com/routing/7.2/calculateroute.json")
                .Append($"?app_id={_hereSettings.AppId}")
                .Append($"&app_code={_hereSettings.AppCode}")
                .Append($"&language=ru-ru")
                .Append($"&mode=fastest;{mode}")
                .Append($"&maneuverattributes=po,ti,pt,ac,di,fj,ix")
                .Append($"&routeattributes=sh,gr")
                .Append($"&waypoint0=geo!stopOver!{from.Latitude.ToString(NFI)},{from.Longitude.ToString(NFI)}")
                .Append($"&waypoint1=geo!stopOver!{to.Latitude.ToString(NFI)},{to.Longitude.ToString(NFI)}")
                .Append($"&departure={time.ToString("yyyy-MM-ddTHH:mm:ss")}")
                .ToString();

            return await HttpHepler.GetResult<HereRouteResponse>(url);
        }
    }
}
