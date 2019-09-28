using System;

namespace Api.Models
{
    public class CellInfoSaveRequest : CellPoint
    {
        
        public string IMEI { get; set; }
        
        public DateTime Timestamp { get; set; }

        public CellInfoSaveRequest()
        {
            
        }

        public CellInfoSaveRequest(CellInfoMongoModel x) : base(x)
        {
            IMEI = x.IMEI;
            Timestamp = x.Timestamp;
        }
    }
}